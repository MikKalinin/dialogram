package org.kollmir.dialogram

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*
import kotlin.time.Clock

class ChatClient(private val httpClient: HttpClient) {
    private var session: DefaultClientWebSocketSession? = null

    suspend fun connect() {
        if (session == null) {
            session = httpClient.webSocketSession(
                method = HttpMethod.Get,
                host = "127.0.0.1",
                port = SERVER_PORT,
                path = "/chat"
            )
        }
    }
    suspend fun sendMessage(userName: String, text: String) {
        val message = ChatMessage(
            sender = userName,
            text = text,
            id = Clock.System.now().toEpochMilliseconds().toString(),
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        try {
            session?.sendSerialized(message)
        } catch (e: Exception) {
            println("Ошибка отправки сообщения: ${e.message}")
        }
    }

    suspend fun login(username: String, password: String): LoginResponse {
        return try {
            httpClient.post("http://127.0.0.1:${SERVER_PORT}/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }.body()
        } catch (e: Exception) {
            LoginResponse(false, "Ошибка сети: ${e.message}")
        }
    }

    fun observeMessages(): Flow<ChatMessage> = flow {
        try {
            session?.let { currentSession ->
                while(true) {
                    val received = currentSession.receiveDeserialized<ChatMessage>()
                    emit(received)
                }
            }
        } catch (e: Exception) {
            println("Соединение закрыто")
        }
    }

    fun disconnect() {
        session = null
    }
}