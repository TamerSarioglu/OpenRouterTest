package com.tamersarioglu.openapitest.presentation.chat

import androidx.lifecycle.viewModelScope
import com.tamersarioglu.openapitest.domain.model.ChatMessage
import com.tamersarioglu.openapitest.domain.model.ChatRequest
import com.tamersarioglu.openapitest.domain.repository.OpenRouterRepository
import com.tamersarioglu.openapitest.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: OpenRouterRepository,
) : BaseViewModel<ChatState, ChatEvent, ChatEffect>(ChatState()) {
    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.ApiKeyChanged -> updateApiKey(event.value)
            is ChatEvent.QuestionChanged -> updateQuestion(event.value)
            ChatEvent.SubmitClicked -> submitQuestion()
        }
    }

    private fun updateApiKey(value: String) {
        setState { copy(apiKey = value) }
    }

    private fun updateQuestion(value: String) {
        setState { copy(question = value) }
    }

    private fun submitQuestion() {
        val state = currentState
        if (!state.canSubmit) {
            sendEffect(ChatEffect.ShowMessage("API key ve soru bos olamaz."))
            return
        }

        setState {
            copy(
                responseText = "",
                isLoading = true,
            )
        }

        viewModelScope.launch {
            val streamedResponse = StringBuilder()
            val request = ChatRequest(
                apiKey = state.apiKey.trim(),
                model = DEFAULT_MODEL,
                messages = listOf(
                    ChatMessage(
                        role = "user",
                        content = state.question,
                    )
                ),
            )
            val result = withContext(Dispatchers.IO) {
                repository.askQuestion(
                    request = request,
                    onChunkReceived = { chunk ->
                        streamedResponse.append(chunk)
                        setState { copy(responseText = responseText + chunk) }
                    },
                )
            }

            result
                .onSuccess { response ->
                    setState {
                        copy(
                            responseText = streamedResponse.toString()
                                .ifBlank { response.content }
                                .ifBlank { "Empty response received." },
                            isLoading = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Istek basarisiz oldu."
                    setState {
                        copy(
                            responseText = message,
                            isLoading = false,
                        )
                    }
                    sendEffect(ChatEffect.ShowMessage(message))
                }
        }
    }

    private companion object {
        const val DEFAULT_MODEL = "gemma-4-26b-a4b-it:free"
    }
}
