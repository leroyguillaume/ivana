package io.ivana.core

import java.util.*

interface OwnableEntity : Entity {
    val ownerId: UUID
}
