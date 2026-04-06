package org.kollmir.dialogram

import org.mindrot.jbcrypt.BCrypt

object HashCoder {
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    fun verifyPassword(password: String, hashed: String): Boolean {
        return try {
            BCrypt.checkpw(password, hashed)
        } catch (e: Exception) {
            false
        }
    }
}


