package io.ivana.core

import java.util.*

interface AlbumEventRepository : EventRepository<AlbumEvent> {
    fun saveCreationEvent(name: String, source: EventSource.User): AlbumEvent.Creation

    fun saveUpdateEvent(id: UUID, content: AlbumEvent.Update.Content, source: EventSource.User): AlbumEvent.Update
}
