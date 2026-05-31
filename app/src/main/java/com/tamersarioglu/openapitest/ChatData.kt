package com.tamersarioglu.openapitest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val reasoning: ReasoningConfig? = null,
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    val seed: Int? = null,
    @SerialName("response_format")
    val responseFormat: ResponseFormat? = null,
    val tools: List<Tool>? = null,
    @SerialName("tool_choice")
    val toolChoice: JsonElement? = null
)

@Serializable
data class ReasoningConfig(
    val enabled: Boolean = true,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    val exclude: Boolean? = null
)

@Serializable
data class ResponseFormat(
    val type: String = "text"
)

@Serializable
data class Tool(
    val type: String,
    val function: ToolFunction? = null
)

@Serializable
data class ToolFunction(
    val name: String,
    val description: String? = null,
    val parameters: Map<String, String>? = null
)


@Serializable
data class StreamDelta(
    val content: String? = null,
    val reasoning: String? = null
)

@Serializable
data class StreamChoice(
    val delta: StreamDelta? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class UsageInfo(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0,
    @SerialName("reasoning_tokens")
    val reasoningTokens: Int? = null
)

@Serializable
data class StreamResponse(
    val id: String? = null,
    val choices: List<StreamChoice> = emptyList(),
    val usage: UsageInfo? = null,
    val model: String? = null
)
