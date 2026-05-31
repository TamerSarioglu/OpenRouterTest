package com.tamersarioglu.openapitest.data.remote

import android.util.Log
import com.tamersarioglu.openapitest.data.mapper.OpenRouterRemoteResult
import com.tamersarioglu.openapitest.data.mapper.toDto
import com.tamersarioglu.openapitest.data.remote.api.OpenRouterApi
import com.tamersarioglu.openapitest.domain.model.ChatRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.ResponseBody
import java.io.IOException
import javax.inject.Inject

class OpenRouterRemoteDataSource @Inject constructor(
    private val api: OpenRouterApi,
    private val json: Json,
) {
    fun askQuestion(
        request: ChatRequest,
        onChunkReceived: (String) -> Unit,
    ): Result<OpenRouterRemoteResult> {
        return try {
            val response = api.createChatCompletion(
                apiKey = "Bearer ${request.apiKey}",
                referer = REFERER,
                appName = APP_NAME,
                request = request.toDto(),
            ).execute()

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "Bilinmeyen hata"
                Log.e(TAG, "OpenRouter error response: code=${response.code()}, body=$errorBody")
                return Result.failure(IllegalStateException("HTTP ${response.code()}: $errorBody"))
            }

            val body = response.body() ?: throw IOException("Bos yanit")
            Result.success(parseChatResponse(body, request.model, onChunkReceived))
        } catch (e: IOException) {
            Log.e(TAG, "Network error while calling OpenRouter", e)
            Result.failure(IOException("Ag hatasi: ${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while calling OpenRouter", e)
            Result.failure(e)
        }
    }

    private fun parseChatResponse(
        body: ResponseBody,
        model: String,
        onChunkReceived: (String) -> Unit,
    ): OpenRouterRemoteResult {
        val fullContent = StringBuilder()
        var reasoningTokens: Int? = null
        var totalTokens: Int? = null

        try {
            val responseText = body.string()
            val lines = responseText.lineSequence().ifEmpty { sequenceOf(responseText) }

            for (line in lines) {
                if (line.isBlank() || !line.startsWith("data:")) continue

                val jsonData = line.removePrefix("data:").trim()
                if (jsonData == "[DONE]") break

                runCatching {
                    val parsed = appendChatResponse(jsonData, fullContent, onChunkReceived)
                    reasoningTokens = parsed.reasoningTokens ?: reasoningTokens
                    totalTokens = parsed.totalTokens ?: totalTokens
                }.onFailure {
                    Log.e(TAG, "Failed to parse SSE JSON chunk: ${jsonData.take(1000)}", it)
                }
            }

            if (fullContent.isEmpty() && responseText.isNotBlank() && !responseText.startsWith("data:")) {
                val parsed = appendChatResponse(responseText, fullContent, onChunkReceived)
                reasoningTokens = parsed.reasoningTokens ?: reasoningTokens
                totalTokens = parsed.totalTokens ?: totalTokens
            }
        } finally {
            body.close()
        }

        return OpenRouterRemoteResult(
            content = fullContent.toString(),
            reasoningTokens = reasoningTokens,
            totalTokens = totalTokens,
            model = model,
        )
    }

    private fun appendChatResponse(
        jsonData: String,
        fullContent: StringBuilder,
        onChunkReceived: (String) -> Unit,
    ): ParsedUsage {
        val chatResponse = json.parseToJsonElement(jsonData).jsonObject
        val choice = chatResponse["choices"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
        val delta = choice?.get("delta")?.jsonObject
        val message = choice?.get("message")?.jsonObject

        val reasoning = delta?.stringOrNull("reasoning")
            ?: message?.stringOrNull("reasoning")
        if (!reasoning.isNullOrEmpty()) {
            onChunkReceived("[REASONING] $reasoning\n\n")
        }

        val content = delta?.stringOrNull("content")
            ?: message?.stringOrNull("content")
            ?: choice?.stringOrNull("text")
        if (!content.isNullOrEmpty()) {
            fullContent.append(content)
            onChunkReceived(content)
        }

        val usage = chatResponse["usage"]?.jsonObject
        return ParsedUsage(
            reasoningTokens = usage?.intOrNull("reasoning_tokens"),
            totalTokens = usage?.intOrNull("total_tokens"),
        )
    }

    private data class ParsedUsage(
        val reasoningTokens: Int?,
        val totalTokens: Int?,
    )

    private companion object {
        const val TAG = "OpenRouterRemoteDataSource"
        const val REFERER = "http://localhost"
        const val APP_NAME = "Kotlin-Test-App"
    }
}

private fun JsonObject.stringOrNull(key: String): String? {
    return this[key]?.jsonPrimitive?.contentOrNull
}

private fun JsonObject.intOrNull(key: String): Int? {
    return this[key]?.jsonPrimitive?.intOrNull
}
