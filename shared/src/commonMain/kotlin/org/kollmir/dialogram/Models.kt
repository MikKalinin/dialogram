package org.kollmir.dialogram

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val sender: String,
    val text: String,
    val timestamp: Long
)

@Serializable
data class LoginRequest(
    val username: String,
    val passwordHash: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val useId: String? = null
)