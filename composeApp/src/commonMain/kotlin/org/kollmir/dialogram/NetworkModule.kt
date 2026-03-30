package org.kollmir.dialogram

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json

val globalHttpClient = HttpClient {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
}

val chatClient = ChatClient(globalHttpClient)