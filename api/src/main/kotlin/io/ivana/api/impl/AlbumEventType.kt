package io.ivana.api.impl

enum class AlbumEventType : EventType {
    Creation,
    Deletion,
    Update;

    override val sqlValue = name.toLowerCase()
    override val sqlType = "album_event_type"
}
