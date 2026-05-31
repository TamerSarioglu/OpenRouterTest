package com.tamersarioglu.openapitest.domain.model

data class ChatResponse(
    val content: String,
    val reasoningTokens: Int?,
    val totalTokens: Int?,
    val model: String?,
)
