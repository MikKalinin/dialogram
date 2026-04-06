package org.kollmir.dialogram

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class ChatClient(private val httpClient: HttpClient) {
    private val client = HttpClient {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    private var session: DefaultClientWebSocketSession? = null

    suspend fun connect() {
        session = client.webSocketSession(
            method = HttpMethod.Get,
            host  = "localhost",
            port = SERVER_PORT,
            path = "/chat"
        )
    }

    suspend fun sendMessage(userName: String, text: String) {
        val message = ChatMessage(
            sender = userName,
            text = text,
            id = "1",
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        session?.sendSerialized(message)
    }

    suspend fun login(username: String, password: String): LoginResponse {
        return try {
            httpClient.post("http://localhost:${SERVER_PORT}/login") {
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
}