package io.ivana.core

import java.util.*

data class User(
    override val id: UUID,
    val name: String,
    val hashedPwd: String
) : Entity
