package io.ivana.core

import java.util.*

interface PhotoRepository : OwnableEntityRepository<Photo> {
    fun fetchAllOfAlbum(albumId: UUID, userId: UUID, offset: Int, limit: Int): List<Photo>

    fun fetchByHash(ownerId: UUID, hash: String): Photo?

    fun fetchNextOf(no: Int): Photo?

    fun fetchNextOf(no: Int, userId: UUID): Photo?

    fun fetchNextOf(order: Int, userId: UUID, albumId: UUID): Photo?

    fun fetchPreviousOf(no: Int): Photo?

    fun fetchPreviousOf(no: Int, userId: UUID): Photo?

    fun fetchPreviousOf(order: Int, userId: UUID, albumId: UUID): Photo?
}
