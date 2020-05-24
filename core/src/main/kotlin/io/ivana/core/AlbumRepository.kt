package io.ivana.core

import java.util.*

interface AlbumRepository : OwnableEntityRepository<Album> {
    fun fetchDuplicateIds(id: UUID, photosIds: Set<UUID>): Set<UUID>

    fun fetchOrder(id: UUID, photoId: UUID): Int?

    fun fetchSize(id: UUID, userId: UUID): Int?
}
