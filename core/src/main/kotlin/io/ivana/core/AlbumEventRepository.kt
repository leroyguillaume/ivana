package io.ivana.core

import java.util.*

interface AlbumEventRepository : EventRepository<AlbumEvent> {
    fun saveCreationEvent(name: String, source: EventSource.User): AlbumEvent.Creation

    fun saveDeletionEvent(albumId: UUID, source: EventSource.User): AlbumEvent.Deletion

    fun saveUpdateEvent(albumId: UUID, content: AlbumEvent.Update.Content, source: EventSource.User): AlbumEvent.Update

    fun saveUpdatePermissionsEvent(
        albumId: UUID,
        content: AlbumEvent.UpdatePermissions.Content,
        source: EventSource.User
    ): AlbumEvent.UpdatePermissions
}
