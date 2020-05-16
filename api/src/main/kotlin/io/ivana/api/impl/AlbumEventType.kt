package io.ivana.api.impl

enum class AlbumEventType(
    override val sqlValue: String
) : EventType {
    Creation("creation"),
    Deletion("deletion"),
    Update("update"),
    UpdatePermissions("update_permissions");

    override val sqlType = "album_event_type"
}
