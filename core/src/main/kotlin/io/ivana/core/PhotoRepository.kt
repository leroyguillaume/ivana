package io.ivana.core

import java.util.*

interface PhotoRepository : OwnableEntityRepository<Photo> {
    fun fetchByHash(ownerId: UUID, hash: String): Photo?

    fun fetchNextOf(photo: Photo): Photo?

    fun fetchPreviousOf(photo: Photo): Photo?
}
