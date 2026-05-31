package com.tamersarioglu.openapitest.data.mapper

import com.tamersarioglu.openapitest.data.remote.dto.ChatMessageDto
import com.tamersarioglu.openapitest.data.remote.dto.ChatRequestDto
import com.tamersarioglu.openapitest.data.remote.dto.ReasoningConfigDto
import com.tamersarioglu.openapitest.data.remote.dto.ResponseFormatDto
import com.tamersarioglu.openapitest.domain.model.ChatMessage
import com.tamersarioglu.openapitest.domain.model.ChatRequest
import com.tamersarioglu.openapitest.domain.model.ChatResponse

fun ChatMessage.toDto(): ChatMessageDto {
    return ChatMessageDto(
        role = role,
        content = content,
    )
}

fun ChatRequest.toDto(): ChatRequestDto {
    return ChatRequestDto(
        model = model,
        messages = messages.map { it.toDto() },
        stream = false,
        reasoning = ReasoningConfigDto(
            enabled = true,
            maxTokens = null,
            exclude = false,
        ),
        temperature = 1.0,
        topP = 1.0,
        seed = 1,
        responseFormat = ResponseFormatDto(type = "text"),
    )
}

fun OpenRouterRemoteResult.toDomain(): ChatResponse {
    return ChatResponse(
        content = content,
        reasoningTokens = reasoningTokens,
        totalTokens = totalTokens,
        model = model,
    )
}

data class OpenRouterRemoteResult(
    val content: String,
    val reasoningTokens: Int?,
    val totalTokens: Int?,
    val model: String?,
)
