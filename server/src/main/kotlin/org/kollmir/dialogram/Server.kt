package org.kollmir.dialogram

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.util.concurrent.CopyOnWriteArraySet

val connections = CopyOnWriteArraySet<DefaultWebSocketServerSession>()
val users = mutableMapOf("admin" to HashCoder.hashPassword("1234"), "user1" to HashCoder.hashPassword("qwerty"))

fun main() {
    System.setOut(java.io.PrintStream(System.`out`, true, "UTF-8"))

    embeddedServer(Netty, port = SERVER_PORT) {
        install(CORS) {
            anyHost()

            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Get)

            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)

            allowNonSimpleContentTypes = true
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        install(CallLogging) {
            level = Level.TRACE
        }

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
                try {
                    val request = call.receive<LoginRequest>()
                    val userHashFromDb = users[request.username]

                    if (userHashFromDb != null && HashCoder.verifyPassword(request.passwordHash, userHashFromDb)) {
                        call.respond(LoginResponse(true, "Добро пожаловать!"))
                    } else {
                        call.respond(LoginResponse(false, "Неверный логин или пароль"))
                    }
                } catch (e: Exception) {
                    println("ОШИБКА ДЕСЕРИАЛИЗАЦИИ: ${e.localizedMessage}")
                    call.respond(HttpStatusCode.BadRequest, "Неверный формат данных")
                }
            }
        }
    }.start(wait = true)
}