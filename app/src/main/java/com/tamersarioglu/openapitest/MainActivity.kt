package com.tamersarioglu.openapitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tamersarioglu.openapitest.presentation.chat.ChatScreen
import com.tamersarioglu.openapitest.ui.theme.OpenApiTestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenApiTestTheme {
                ChatScreen()
            }
        }
    }
}
