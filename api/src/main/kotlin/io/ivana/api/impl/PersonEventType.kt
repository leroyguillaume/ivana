package io.ivana.api.impl

enum class PersonEventType(
    override val sqlValue: String
) : EventType {
    Creation("creation"),
    Update("update"),
    Deletion("deletion");

    override val sqlType = "person_event_type"
}
