package io.ivana.core

import java.util.*

interface OwnableEntityRepository<E : OwnableEntity> : EntityRepository<E> {
    fun count(ownerId: UUID): Int

    fun countShared(userId: UUID): Int

    fun fetchAll(ownerId: UUID, offset: Int, limit: Int): List<E>

    fun fetchShared(userId: UUID, offset: Int, limit: Int): List<E>
}
