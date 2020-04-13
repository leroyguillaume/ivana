package io.ivana.core

import java.time.OffsetDateTime
import java.util.*

data class User(
    override val id: UUID,
    val name: String,
    val hashedPwd: String,
    val role: Role,
    val creationDate: OffsetDateTime
) : Entity
