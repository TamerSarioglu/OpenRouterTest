package com.tamersarioglu.openapitest.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamDeltaDto(
    val content: String? = null,
    val reasoning: String? = null,
)

@Serializable
data class StreamChoiceDto(
    val delta: StreamDeltaDto? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@Serializable
data class UsageInfoDto(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0,
    @SerialName("reasoning_tokens")
    val reasoningTokens: Int? = null,
)

@Serializable
data class StreamResponseDto(
    val id: String? = null,
    val choices: List<StreamChoiceDto> = emptyList(),
    val usage: UsageInfoDto? = null,
    val model: String? = null,
)
