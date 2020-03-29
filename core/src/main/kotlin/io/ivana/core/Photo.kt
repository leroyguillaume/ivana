package io.ivana.core

import java.time.OffsetDateTime
import java.util.*

data class Photo(
    override val id: UUID,
    val ownerId: UUID,
    val uploadDate: OffsetDateTime,
    val type: Type,
    val hash: String,
    val no: Int
) : Entity {
    enum class Type {
        Jpg,
        Png
    }
}
