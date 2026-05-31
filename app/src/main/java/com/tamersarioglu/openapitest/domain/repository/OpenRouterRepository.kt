package com.tamersarioglu.openapitest.domain.repository

import com.tamersarioglu.openapitest.domain.model.ChatRequest
import com.tamersarioglu.openapitest.domain.model.ChatResponse

interface OpenRouterRepository {
    fun askQuestion(
        request: ChatRequest,
        onChunkReceived: (String) -> Unit,
    ): Result<ChatResponse>
}
