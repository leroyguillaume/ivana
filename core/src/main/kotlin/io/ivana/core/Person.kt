package io.ivana.core

import java.util.*

data class Person(
    override val id: UUID,
    val lastName: String,
    val firstName: String
) : Entity
