package org.kollmir.dialogram

import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.concurrent.CopyOnWriteArraySet

val connections = CopyOnWriteArraySet<DefaultWebSocketServerSession>()
val users = mutableMapOf("admin" to "1234", "user1" to "qwerty")

fun main() {
    System.setOut(java.io.PrintStream(System.`out`, true, "UTF-8"))

    embeddedServer(Netty, port = SERVER_PORT) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }

        routing {
            webSocket("/chat") {
                connections += this
                try {
                    while (true) {
                        val message = receiveDeserialized<ChatMessage>()
                        println("СЕРВЕР ПОЛУЧИЛ: ${message.text}")
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
            post("/login") {
                val request = call.receive<LoginRequest>()
                val userHashFromDb = users[request.username]

                if (userHashFromDb != null && HashCoder.verifyPassword(request.passwoedHash, userHashFromDb)) {
                    call.respond(LoginResponse(true, "Добро пожаловать!"))
                } else {
                    call.respond(LoginResponse(false, "Неверный логин или пароль"))
                }
            }
        }
    }.start(wait = true)
}