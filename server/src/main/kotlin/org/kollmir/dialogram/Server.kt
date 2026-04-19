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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
fun initDatabase() {
    Database.connect("jdbc:sqlite:./data.db", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(Users, Messages)

        val adminExists = Users.select { Users.username eq "admin" }.empty()
        if (adminExists) {
            Users.insert {
                it[username] = "admin"
                it[passwordHash] = HashCoder.hashPassword("toor")
            }
        }
    }
}


fun main() {
    System.setOut(java.io.PrintStream(System.`out`, true, "Windows-1252"))
    initDatabase()
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0") {
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

                        transaction {
                            Messages.insert {
                                it[sender] = message.sender
                                it[text] = message.text
                                it[timestamp] = LocalDateTime.now()
                            }
                        }

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
            get("/history") {
                val history = transaction {
                    Messages.selectAll()
                        .orderBy(Messages.timestamp to SortOrder.ASC)
                        .limit(50)
                        .map {
                            ChatMessage(sender = it[Messages.sender], text = it[Messages.text])
                        }
                }
                call.respond(history)
            }
            post("/login") {
                try {
                    val request = call.receive<LoginRequest>()
                    val userFromDb = transaction {
                        Users.select { Users.username eq request.username }
                            .map { it[Users.passwordHash] }
                            .singleOrNull()
                    }

                    if (userFromDb != null && HashCoder.verifyPassword(request.passwordHash, userFromDb)) {
                        call.respond(LoginResponse(true, "Добро пожаловать!"))
                    } else {
                        call.respond(LoginResponse(false, "Неверный логин или пароль"))
                    }
                } catch (e: Exception) {
                    println("ОШИБКА ДЕСЕРИАЛИЗАЦИИ: ${e.localizedMessage}")
                    call.respond(HttpStatusCode.BadRequest, "Неверный формат данных")
                }
            }
            post("/register") {
                val request = call.receive<LoginRequest>()

                val userExists = transaction {
                    Users.select { Users.username eq request.username }.any()
                }

                if (userExists) {
                    call.respond(LoginResponse(false, "Пользователь уже существует"))
                } else {
                    transaction {
                        Users.insert {
                            it[username] = request.username
                            it[passwordHash] = HashCoder.hashPassword(request.passwordHash)
                        }
                    }
                    call.respond(LoginResponse(true, "Регистрация успешна!"))
                }
            }
        }
    }.start(wait = true)
}