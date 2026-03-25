package org.kollmir.dialogram

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val sender: String,
    val text: String,
    val timestamp: Long
)