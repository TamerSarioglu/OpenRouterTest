package com.tamersarioglu.openapitest.presentation.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ChatEffect.ShowMessage -> snackBarHostState.showSnackbar(effect.message)
            }
        }
    }

    TestAppScreen(
        state = state,
        snackBarHostState = snackBarHostState,
        onApiKeyChanged = { viewModel.onEvent(ChatEvent.ApiKeyChanged(it)) },
        onQuestionChanged = { viewModel.onEvent(ChatEvent.QuestionChanged(it)) },
        onSubmitClicked = { viewModel.onEvent(ChatEvent.SubmitClicked) },
    )
}

@Composable
fun TestAppScreen(
    state: ChatState,
    snackBarHostState: SnackbarHostState,
    onApiKeyChanged: (String) -> Unit,
    onQuestionChanged: (String) -> Unit,
    onSubmitClicked: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = onApiKeyChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("OpenRouter API Key") },
                singleLine = true,
            )

            OutlinedTextField(
                value = state.question,
                onValueChange = onQuestionChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Question") },
                minLines = 3,
            )

            Button(
                enabled = state.canSubmit,
                onClick = onSubmitClicked,
            ) {
                Text(if (state.isLoading) "Sending..." else "Start Ask Question")
            }

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            Text(
                text = state.responseText,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            )
        }
    }
}
