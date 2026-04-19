package org.kollmir.dialogram

import androidx.compose.runtime.*

enum class Screen { Login, Register, Chat}
@Composable
fun App() {
    val chatClient = remember { ChatClient(host = "192.168.1.131", port = SERVER_PORT) }
    var currentNickname by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf(Screen.Login) }

    when (currentScreen) {
        Screen.Login -> LoginScreen(
            client = chatClient,
            onSuccess = { nickname ->
                currentNickname = nickname
                currentScreen = Screen.Chat },
            onRegisterClick = { currentScreen = Screen.Register }
        )
        Screen.Register -> RegistrationScreen(
            client = chatClient,
            onRegistrationSuccess = { currentScreen = Screen.Login },
            onBackToLogin = { currentScreen = Screen.Login }
        )
        Screen.Chat -> ChatScreen(
            client = chatClient,
            nickname = currentNickname
        )
    }
}