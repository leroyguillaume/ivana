package io.ivana.api.impl

enum class AlbumEventType : EventType {
    Creation,
    Update;

    override val sqlValue = name.toLowerCase()
    override val sqlType = "album_event_type"
}
