package org.kollmir.dialogram

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun LoginScreen(
    client: ChatClient,
    onSuccess: (nickname: String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center) {
        Text("Вход в Dialogram", style = MaterialTheme.typography.headlineMedium)
        TextField(username, { username = it }, label = { Text("Логин") })
        TextField(password, { password = it }, label = { Text("Пароль")} )

        if (errorText.isNotEmpty()) {
            Text(errorText, color = androidx.compose.ui.graphics.Color.Red)
        }

        Button(onClick =  {
            scope.launch {
                val response = client.login(username, password)
                if (response.success) {
                    onSuccess(username)
                } else {
                    errorText = response.message
                }
            }
        }) {
            Text("Войти")
        }

        TextButton(onClick = onRegisterClick) {
            Text("Нет аккаунта? Зарегистрируйтесь")
        }
    }
}