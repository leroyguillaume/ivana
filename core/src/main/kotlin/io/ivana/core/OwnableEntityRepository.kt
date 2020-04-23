package io.ivana.core

import java.util.*

interface OwnableEntityRepository<E : OwnableEntity> : EntityRepository<E> {
    fun count(ownerId: UUID): Int

    fun fetchAll(ownerId: UUID, offset: Int, limit: Int): List<E>
}
