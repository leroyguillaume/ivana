package io.ivana.core

import java.util.*

interface EntityRepository<E : Entity> {
    fun existsById(id: UUID): Boolean

    fun fetchById(id: UUID): E?
}
