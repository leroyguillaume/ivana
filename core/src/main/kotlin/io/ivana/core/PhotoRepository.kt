package io.ivana.core

import java.util.*

interface PhotoRepository : OwnableEntityRepository<Photo> {
    fun countOfAlbum(albumId: UUID, userId: UUID): Int

    fun fetchAllOfAlbum(albumId: UUID, userId: UUID, offset: Int, limit: Int): List<Photo>

    fun fetchByHash(ownerId: UUID, hash: String): Photo?

    fun fetchNextOf(photo: Photo): Photo?

    fun fetchPreviousOf(photo: Photo): Photo?
}
