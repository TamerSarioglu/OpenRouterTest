package com.tamersarioglu.openapitest.data.repository

import com.tamersarioglu.openapitest.data.mapper.toDomain
import com.tamersarioglu.openapitest.data.remote.OpenRouterRemoteDataSource
import com.tamersarioglu.openapitest.domain.model.ChatRequest
import com.tamersarioglu.openapitest.domain.model.ChatResponse
import com.tamersarioglu.openapitest.domain.repository.OpenRouterRepository
import javax.inject.Inject

class OpenRouterRepositoryImpl @Inject constructor(
    private val remoteDataSource: OpenRouterRemoteDataSource,
) : OpenRouterRepository {
    override fun askQuestion(
        request: ChatRequest,
        onChunkReceived: (String) -> Unit,
    ): Result<ChatResponse> {
        return remoteDataSource.askQuestion(
            request = request,
            onChunkReceived = onChunkReceived,
        ).map { it.toDomain() }
    }
}
