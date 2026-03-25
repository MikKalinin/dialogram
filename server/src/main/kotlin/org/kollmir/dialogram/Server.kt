package org.kollmir.dialogram

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

val connections = CopyOnWriteArraySet<DefaultWebSocketServerSession>()

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(WebSockets)

        routing {
            webSocket("/chat") {
                connections += this
                try {
                    send("Вы подключены к Dialogram!")

                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val text = frame.readText()

                        connections.forEach { session ->
                            session.send("Кто-то сказал: $text")
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