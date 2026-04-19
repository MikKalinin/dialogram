package org.kollmir.dialogram

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

class ChatClient(private val host: String, private val port: Int) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)

        }
    }
    private val baseUrl = "http://$host:$port"
    private var session: DefaultClientWebSocketSession? = null

    suspend fun login(username: String, password: String): LoginResponse {
        return try {
            client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }.body()
        } catch (e: Exception) {
            LoginResponse(false, "Ошибка сети: ${e.message}")
        }
    }

    suspend fun register(username: String, passwordHash: String): LoginResponse {
        return try {
            client.post("$baseUrl/register") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, passwordHash))
            }.body()
        } catch (e: Exception) {
            LoginResponse(false, "Ошибка регистрации: ${e.message}")
        }
    }

    suspend fun getHistory(): List<ChatMessage> {
        return try {
            client.get("$baseUrl/history").body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun connectToChat(): Flow<ChatMessage> = flow {
        try {
            val activeSession = client.webSocketSession(method = HttpMethod.Get, host = host, port = port, path = "/chat")
            session = activeSession

            while (true) {
                val message = activeSession.receiveDeserialized<ChatMessage>()
                emit(message)
            }
        } catch (e: Exception) {
            println("Websocket error: ${e.message}")
            session = null
        }
    }
    suspend fun sendMessage(userName: String, text: String) {
        val message = ChatMessage(
            sender = userName,
            text = text,
        )
        try {
            session?.sendSerialized(message)
        } catch (e: Exception) {
            println("Ошибка отправки: ${e.message}")
        }
    }

    fun close() {
        client.close()
    }
}