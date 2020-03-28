package io.ivana.api.impl

enum class UserEventType : EventType {
    Creation,
    Login;

    override val sqlValue = name.toLowerCase()
    override val sqlType = "user_event_type"
}
