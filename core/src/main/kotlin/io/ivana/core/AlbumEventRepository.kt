package io.ivana.core

interface AlbumEventRepository : EventRepository<AlbumEvent> {
    fun saveCreationEvent(name: String, source: EventSource.User): AlbumEvent.Creation
}
