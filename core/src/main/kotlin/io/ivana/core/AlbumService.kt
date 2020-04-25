package io.ivana.core

import java.util.*

interface AlbumService : OwnableEntityService<Album> {
    fun create(name: String, source: EventSource.User): Album

    fun getAllPhotos(id: UUID, pageNo: Int, pageSize: Int): Page<Photo>
}