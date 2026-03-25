package org.kollmir.dialogram

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*

class ChatClient {
    private val client = HttpClient {
        install(WebSockets)
    }

    private var session: WebSocketSession? = null

    suspend fun connect() {
        session = client.webSocketSession(
            method = HttpMethod.Get,
            host  = "localhost",
            port = 8080,
            path = "/chat"
        )
    }

    suspend fun sendMessage(text: String) {
        session?.send(Frame.Text(text))
    }

    fun observeMessages(): Flow<String> = flow {
        session?.incoming?.receiveAsFlow()?.collect { frame ->
            if (frame is Frame.Text) {
                emit(frame.readText())
            }
        }
    }
}