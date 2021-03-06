package io.ivana.core

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

data class Photo(
    override val id: UUID,
    override val ownerId: UUID,
    val uploadDate: OffsetDateTime,
    val shootingDate: LocalDate? = null,
    val type: Type,
    val hash: String,
    val no: Int,
    val version: Int
) : OwnableEntity {
    enum class Type {
        Jpg,
        Png
    }
}
