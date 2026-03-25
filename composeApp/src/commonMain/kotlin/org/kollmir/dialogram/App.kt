package org.kollmir.dialogram

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import kotlinx.coroutines.launch

@Composable
fun App() {
    val client = remember { ChatClient() }
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        try {
            client.connect()
            client.observeMessages().collect { msg ->
                messages.add(msg)
            }
        } catch (e: Exception) {
            messages.add("Ошибка подключения: ${e.message}")
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { msg ->
                Text(msg, modifier = Modifier.padding(4.dp))
            }
        }

        Row {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                scope.launch {
                    client.sendMessage(messageText)
                    messageText = ""
                }
            }) {
                Text("Отправить")
            }
        }
    }
}