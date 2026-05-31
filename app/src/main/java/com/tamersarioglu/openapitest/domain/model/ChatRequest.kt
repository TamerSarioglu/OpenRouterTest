package com.tamersarioglu.openapitest.domain.model

data class ChatRequest(
    val apiKey: String,
    val model: String,
    val messages: List<ChatMessage>,
)
