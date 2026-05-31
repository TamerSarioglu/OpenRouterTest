package com.tamersarioglu.openapitest.presentation.chat

data class ChatState(
    val apiKey: String = "",
    val question: String = "Merhaba, omlet tarifi yapar misin?",
    val responseText: String = "",
    val isLoading: Boolean = false,
) {
    val canSubmit: Boolean
        get() = !isLoading && apiKey.isNotBlank() && question.isNotBlank()
}

sealed interface ChatEvent {
    data class ApiKeyChanged(val value: String) : ChatEvent
    data class QuestionChanged(val value: String) : ChatEvent
    data object SubmitClicked : ChatEvent
}

sealed interface ChatEffect {
    data class ShowMessage(val message: String) : ChatEffect
}
