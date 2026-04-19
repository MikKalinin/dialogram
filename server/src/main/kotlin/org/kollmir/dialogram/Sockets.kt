package org.kollmir.dialogram

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.LinkedHashSet

val connections = Collections.synchronizedSet<DefaultWebSocketServerSession?>(LinkedHashSet())

fun Application.configureSockets() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    routing {
        webSocket("/chat") {
            connections.add(this)

            try {
                for (frame in incoming) {
                    val message = receiveDeserialized<ChatMessage>()

                    println("Получено сообщение от ${message.sender}: ${message.text}")

                    val allConnections = connections.toList()
                    allConnections.forEach{ session ->
                        try {
                            session.sendSerialized(message)
                        } catch (e: Exception) {
                            println("Ошибка отправки клиенту: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Ошибка в WebSocket сессии: ${e.message}")
            } finally {
                println("Клиент отключился: $this")
                connections.remove(this)
            }
        }
    }
}