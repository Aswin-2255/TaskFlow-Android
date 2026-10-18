package com.example.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskflow.ui.AuthScreen
import com.example.taskflow.ui.HomeScreen
import com.example.taskflow.ui.theme.TaskflowTheme
import com.example.taskflow.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskflowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF1F5F9)
                ) {
                    TaskflowApp()
                }
            }
        }
    }
}

@Composable
fun TaskflowApp(viewModel: TaskViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.isAuthScreenVisible,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "authScreenTransition"
    ) { isAuthVisible ->
        if (isAuthVisible) {
            AuthScreen(
                onLoginSuccess = { email, name ->
                    viewModel.login(email = email, name = name)
                },
                onGuestContinue = {
                    viewModel.hideAuthScreen()
                }
            )
        } else {
            HomeScreen(viewModel = viewModel)
        }
    }
}
