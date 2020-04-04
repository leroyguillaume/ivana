package io.ivana.core

import java.util.*

interface PhotoRepository : EntityRepository<Photo> {
    fun count(ownerId: UUID): Int

    fun fetchAll(ownerId: UUID, offset: Int, limit: Int): List<Photo>

    fun fetchByHash(ownerId: UUID, hash: String): Photo?

    fun fetchNextOf(photo: Photo): Photo?

    fun fetchPreviousOf(photo: Photo): Photo?
}
