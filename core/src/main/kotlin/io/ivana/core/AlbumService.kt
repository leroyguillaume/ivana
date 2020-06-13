package io.ivana.core

import java.util.*

interface AlbumService : OwnableEntityService<Album> {
    fun create(name: String, source: EventSource.User): Album

    fun delete(id: UUID, source: EventSource.User)

    fun getAllPhotos(id: UUID, userId: UUID, pageNo: Int, pageSize: Int): Page<Photo>

    fun suggest(name: String, count: Int, userId: UUID, perm: Permission): List<Album>

    fun update(id: UUID, content: AlbumEvent.Update.Content, source: EventSource.User): Album

    fun updatePermissions(
        id: UUID,
        permissionsToAdd: Set<UserPermissions>,
        permissionsToRemove: Set<UserPermissions>,
        source: EventSource.User
    )
}
