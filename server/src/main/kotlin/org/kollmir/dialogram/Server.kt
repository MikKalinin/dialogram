package org.kollmir.dialogram

import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.concurrent.CopyOnWriteArraySet

val connections = CopyOnWriteArraySet<DefaultWebSocketServerSession>()

fun main() {
    embeddedServer(Netty, port = SERVER_PORT) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }

        routing {
            webSocket("/chat") {
                connections += this
                try {
                    for (frame in incoming) {
                        val message = receiveDeserialized<ChatMessage>()

                        connections.forEach { session ->
                            session.sendSerialized(message)
                        }
                    }
                } catch (e: Exception) {
                    println("Ошибка: ${e.localizedMessage}")
                } finally {
                    connections -= this
                }
            }
        }
    }.start(wait = true)
}