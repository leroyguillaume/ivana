package io.ivana.api.impl

enum class PhotoEventType(
    override val sqlValue: String
) : EventType {
    Deletion("deletion"),
    Transform("transform"),
    Update("update"),
    UpdatePermissions("update_permissions"),
    Upload("upload"),
    UpdatePeople("update_people");

    override val sqlType = "photo_event_type"
}
