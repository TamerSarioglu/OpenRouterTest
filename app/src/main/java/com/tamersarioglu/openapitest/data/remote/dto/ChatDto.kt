package com.tamersarioglu.openapitest.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String,
)

@Serializable
data class ChatRequestDto(
    val model: String,
    val messages: List<ChatMessageDto>,
    val stream: Boolean = false,
    val reasoning: ReasoningConfigDto? = null,
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    val seed: Int? = null,
    @SerialName("response_format")
    val responseFormat: ResponseFormatDto? = null,
    val tools: List<ToolDto>? = null,
    @SerialName("tool_choice")
    val toolChoice: JsonElement? = null,
)

@Serializable
data class ReasoningConfigDto(
    val enabled: Boolean = true,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    val exclude: Boolean? = null,
)

@Serializable
data class ResponseFormatDto(
    val type: String = "text",
)

@Serializable
data class ToolDto(
    val type: String,
    val function: ToolFunctionDto? = null,
)

@Serializable
data class ToolFunctionDto(
    val name: String,
    val description: String? = null,
    val parameters: Map<String, String>? = null,
)
