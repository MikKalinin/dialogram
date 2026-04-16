package org.kollmir.dialogram

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Messages : Table("messages") {
    val id = integer("id").autoIncrement()
    val sender = varchar("sender", 50)
    val text = text("text")
    val timestamp = datetime("timestamp").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}