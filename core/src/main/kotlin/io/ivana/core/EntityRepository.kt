package io.ivana.core

import java.util.*

interface EntityRepository<E : Entity> {
    fun count(): Int

    fun existsById(id: UUID): Boolean

    fun fetchAll(offset: Int, limit: Int): List<E>

    fun fetchAllByIds(ids: Set<UUID>): Set<E>

    fun fetchById(id: UUID): E?

    fun fetchExistingIds(ids: Set<UUID>): Set<UUID>
}
