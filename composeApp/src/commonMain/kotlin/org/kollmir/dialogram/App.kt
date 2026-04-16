package org.kollmir.dialogram

import androidx.compose.runtime.*

enum class Screen { Login, Register, Chat}
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.Login) }

    when (currentScreen) {
        Screen.Login -> LoginScreen(
            onSuccess = { currentScreen = Screen.Chat },
            onRegisterClick = { currentScreen = Screen.Register }
        )
        Screen.Register -> RegistrationScreen(
            onRegistrationSuccess = { currentScreen = Screen.Login },
            onBackToLogin = { currentScreen = Screen.Login }
        )
        Screen.Chat -> ChatScreen()
    }
}