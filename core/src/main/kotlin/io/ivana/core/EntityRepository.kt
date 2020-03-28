package io.ivana.core

import java.util.*

interface EntityRepository<E : Entity> {
    fun fetchById(id: UUID): E?
}
