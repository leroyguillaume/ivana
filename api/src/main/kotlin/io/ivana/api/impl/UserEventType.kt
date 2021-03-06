package io.ivana.api.impl

enum class UserEventType(
    override val sqlValue: String
) : EventType {
    Creation("creation"),
    Deletion("deletion"),
    Login("login"),
    PasswordUpdate("password_update");

    override val sqlType = "user_event_type"
}
