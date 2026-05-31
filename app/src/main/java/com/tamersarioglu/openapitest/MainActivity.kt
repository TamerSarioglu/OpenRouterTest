package com.tamersarioglu.openapitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tamersarioglu.openapitest.ui.theme.OpenApiTestTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenApiTestTheme {
                TestAppScreen()
            }
        }
    }
}

@Composable
fun TestAppScreen() {
    val scope = rememberCoroutineScope()
    var apiKey by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("Merhaba, omlet tarifi yapar mısın?") }
    var responseText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("OpenRouter API Key") },
                singleLine = true
            )

            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Question") },
                minLines = 3
            )

            Button(
                enabled = !isLoading && apiKey.isNotBlank() && question.isNotBlank(),
                onClick = {
                    isLoading = true
                    responseText = ""

                    scope.launch {
                        val streamedResponse = StringBuilder()
                        val result = withContext(Dispatchers.IO) {
                            OpenRouterClient(apiKey = apiKey.trim()).streamChatSync(
                                model = "gemma-4-26b-a4b-it:free",
                                messages = listOf(
                                    ChatMessage(
                                        role = "user",
                                        content = question
                                    )
                                ),
                                onChunkReceived = { chunk ->
                                    streamedResponse.append(chunk)
                                    scope.launch {
                                        responseText += chunk
                                    }
                                }
                            )
                        }

                        if (!result.success) {
                            responseText = result.errorMessage.orEmpty()
                        } else {
                            responseText = streamedResponse.toString()
                                .ifBlank { result.content }
                                .ifBlank { "Empty response received." }
                        }

                        isLoading = false
                    }
                }
            ) {
                Text(if (isLoading) "Sending..." else "Start Ask Question")
            }

            if (isLoading) {
                CircularProgressIndicator()
            }

            Text(
                text = responseText,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}
