package io.ivana.api.impl

enum class PhotoEventType(
    override val sqlValue: String
) : EventType {
    Deletion("deletion"),
    Transform("transform"),
    UpdatePermissions("update_permissions"),
    Upload("upload");

    override val sqlType = "photo_event_type"
}
