package io.ivana.core

import java.time.OffsetDateTime
import java.util.*

data class Album(
    override val id: UUID,
    override val ownerId: UUID,
    val name: String,
    val creationDate: OffsetDateTime
) : OwnableEntity
