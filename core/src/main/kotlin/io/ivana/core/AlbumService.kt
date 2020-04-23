package io.ivana.core

interface AlbumService : OwnableEntityService<Album> {
    fun create(name: String, source: EventSource.User): Album
}
