package org.kollmir.dialogram

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun App() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var loginUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("User_${(1..100).random()}") }
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isConnected by remember { mutableStateOf(false) }
    val myHttpClient = HttpClient {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    val chatClient = ChatClient(myHttpClient)

    if(!isLoggedIn) {
        Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center) {
            Text("Вход в Dialogram", style = MaterialTheme.typography.headlineMedium)
            TextField(loginUsername, { loginUsername = it }, label = { Text("Логин") })
            TextField(password, { password = it }, label = { Text("Пароль")} )

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = androidx.compose.ui.graphics.Color.Red)
            }

            Button(onClick =  {
                scope.launch {
                    val response = chatClient.login(loginUsername, password)
                    if (response.success) {
                        isLoggedIn = true
                    } else {
                        errorMessage = response.message
                    }
                }
            }) {
                Text("Войти")
            }
        }
    } else {
        LaunchedEffect(Unit) {
            try {
                chatClient.connect()
                isConnected = true
                chatClient.observeMessages().collect { msg ->
                    messages.add(msg)
                }
            } catch (e: Exception) {
                isConnected = false
                println("Ошибка чата: ${e.message}")
            }
        }
        MaterialTheme {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Верхняя панель: Статус и Имя
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isConnected) "● Online" else "○ Offline",
                        color = if (isConnected) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
                    )
                    Spacer(Modifier.width(16.dp))
                    TextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Ваше имя") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Список сообщений
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(messages) { msg ->
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(msg.sender, color = MaterialTheme.colorScheme.primary)
                                Text(msg.text)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Поле ввода и кнопка
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Введите сообщение...") }
                    )
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                scope.launch {
                                    chatClient.sendMessage(userName, inputText)
                                    inputText = ""
                                }
                            }
                        },
                        enabled = isConnected
                    ) {
                        Text("➤")
                    }
                }
            }
        }
    }
}