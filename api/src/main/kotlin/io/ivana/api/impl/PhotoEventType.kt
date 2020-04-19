package io.ivana.api.impl

enum class PhotoEventType : EventType {
    Deletion,
    Transform,
    Upload;

    override val sqlValue = name.toLowerCase()
    override val sqlType = "photo_event_type"
}
