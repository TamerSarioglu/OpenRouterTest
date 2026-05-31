package com.tamersarioglu.openapitest

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.ResponseBody
import java.io.IOException

class OpenRouterClient(
    private val apiKey: String,
    private val referer: String = "http://localhost",
    private val appName: String = "Kotlin-Test-App"
) {
    private companion object {
        const val TAG = "OpenRouterClient"
    }

    private val api: OpenRouterApi = RetrofitClient.create().create(OpenRouterApi::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    data class StreamResult(
        val content: String,
        val reasoningTokens: Int?,
        val totalTokens: Int?,
        val model: String?,
        val success: Boolean,
        val errorMessage: String? = null
    )

    fun streamChatSync(
        model: String,
        messages: List<ChatMessage>,
        onChunkReceived: ((String) -> Unit)? = null
    ): StreamResult {
        val request = ChatRequest(
            model = model,
            messages = messages,
            stream = false,
            reasoning = ReasoningConfig(
                enabled = true,
                maxTokens = 1024,
                exclude = false
            ),
            temperature = 1.0,
            topP = 1.0,
            seed = 1,
            responseFormat = ResponseFormat(type = "text")
        )

        return try {
            Log.d(
                TAG,
                "Sending chat completion: model=$model, messageCount=${messages.size}, " +
                    "stream=${request.stream}, temperature=${request.temperature}, topP=${request.topP}, " +
                    "seed=${request.seed}, reasoning=${request.reasoning}"
            )

            val response = api.createChatCompletion(
                apiKey = "Bearer $apiKey",
                referer = referer,
                appName = appName,
                request = request
            ).execute()

            Log.d(TAG, "HTTP response received: code=${response.code()}, successful=${response.isSuccessful}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "Bilinmeyen hata"
                Log.e(TAG, "OpenRouter error response: code=${response.code()}, body=$errorBody")
                return StreamResult(
                    content = "",
                    reasoningTokens = null,
                    totalTokens = null,
                    model = model,
                    success = false,
                    errorMessage = "HTTP ${response.code()}: $errorBody"
                )
            }

            val body = response.body() ?: throw IOException("Boş yanıt")
            val result = parseChatResponse(body, model, onChunkReceived)
            Log.d(
                TAG,
                "Parsed chat response: success=${result.success}, contentLength=${result.content.length}, " +
                    "reasoningTokens=${result.reasoningTokens}, totalTokens=${result.totalTokens}"
            )
            return result

        } catch (e: IOException) {
            Log.e(TAG, "Network error while calling OpenRouter", e)
            StreamResult(
                content = "",
                reasoningTokens = null,
                totalTokens = null,
                model = model,
                success = false,
                errorMessage = "Ağ hatası: ${e.message}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while calling OpenRouter", e)
            StreamResult(
                content = "",
                reasoningTokens = null,
                totalTokens = null,
                model = model,
                success = false,
                errorMessage = "Beklenmeyen hata: ${e.message}"
            )
        }
    }

    private fun parseChatResponse(
        body: ResponseBody,
        model: String,
        onChunkReceived: ((String) -> Unit)?
    ): StreamResult {
        val fullContent = StringBuilder()
        var reasoningTokens: Int? = null
        var totalTokens: Int? = null

        try {
            val responseText = body.string()
            Log.d(
                TAG,
                "Raw response received: length=${responseText.length}, preview=${responseText.take(500)}"
            )
            val lines = responseText.lineSequence().ifEmpty { sequenceOf(responseText) }

            for (line in lines) {
                if (line.isBlank() || !line.startsWith("data:")) continue

                val jsonData = line.removePrefix("data:").trim()
                if (jsonData == "[DONE]") {
                    Log.d(TAG, "SSE stream completed with [DONE]")
                    break
                }

                try {
                    Log.d(TAG, "Parsing SSE data chunk: ${jsonData.take(500)}")
                    val parsed = appendChatResponse(jsonData, fullContent, onChunkReceived)
                    reasoningTokens = parsed.reasoningTokens ?: reasoningTokens
                    totalTokens = parsed.totalTokens ?: totalTokens

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse SSE JSON chunk: ${jsonData.take(1000)}", e)
                }
            }

            if (fullContent.isEmpty() && responseText.isNotBlank() && !responseText.startsWith("data:")) {
                Log.d(TAG, "Parsing non-streaming JSON response")
                val parsed = appendChatResponse(responseText, fullContent, onChunkReceived)
                reasoningTokens = parsed.reasoningTokens ?: reasoningTokens
                totalTokens = parsed.totalTokens ?: totalTokens
            }
        } finally {
            body.close()
        }

        return StreamResult(
            content = fullContent.toString(),
            reasoningTokens = reasoningTokens,
            totalTokens = totalTokens,
            model = model,
            success = true
        )
    }

    private fun appendChatResponse(
        jsonData: String,
        fullContent: StringBuilder,
        onChunkReceived: ((String) -> Unit)?
    ): ParsedUsage {
        val chatResponse = json.parseToJsonElement(jsonData).jsonObject
        Log.d(TAG, "Top-level response keys: ${chatResponse.keys}")
        val choice = chatResponse["choices"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
        Log.d(TAG, "First choice keys: ${choice?.keys}")
        val delta = choice?.get("delta")?.jsonObject
        val message = choice?.get("message")?.jsonObject
        Log.d(TAG, "Delta keys: ${delta?.keys}; message keys: ${message?.keys}")

        val reasoning = delta?.stringOrNull("reasoning")
            ?: message?.stringOrNull("reasoning")
        if (!reasoning.isNullOrEmpty()) {
            Log.d(TAG, "Reasoning parsed: length=${reasoning.length}, preview=${reasoning.take(300)}")
            onChunkReceived?.invoke("[REASONING] $reasoning\n\n")
        } else {
            Log.d(TAG, "No reasoning field found in response")
        }

        val content = delta?.stringOrNull("content")
            ?: message?.stringOrNull("content")
            ?: choice?.stringOrNull("text")
        if (!content.isNullOrEmpty()) {
            Log.d(TAG, "Content parsed: length=${content.length}, preview=${content.take(300)}")
            fullContent.append(content)
            onChunkReceived?.invoke(content)
        } else {
            Log.w(TAG, "No content found. choice=$choice")
        }

        val usage = chatResponse["usage"]?.jsonObject
        Log.d(TAG, "Usage parsed: $usage")
        return ParsedUsage(
            reasoningTokens = usage?.intOrNull("reasoning_tokens"),
            totalTokens = usage?.intOrNull("total_tokens")
        )
    }

    private data class ParsedUsage(
        val reasoningTokens: Int?,
        val totalTokens: Int?
    )
}

private fun JsonObject.stringOrNull(key: String): String? {
    return this[key]?.jsonPrimitive?.contentOrNull
}

private fun JsonObject.intOrNull(key: String): Int? {
    return this[key]?.jsonPrimitive?.intOrNull
}
