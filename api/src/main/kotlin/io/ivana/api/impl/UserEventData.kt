package io.ivana.api.impl

internal sealed class UserEventData : EventData {
    data class Creation(
        override val source: EventSourceData,
        val content: Content
    ) : UserEventData() {
        data class Content(
            val name: String,
            val hashedPwd: String,
            val role: RoleData
        )
    }

    data class Deletion(
        override val source: EventSourceData
    ) : UserEventData()

    data class Login(
        override val source: EventSourceData.User
    ) : UserEventData()

    data class PasswordUpdate(
        override val source: EventSourceData,
        val content: Content
    ) : UserEventData() {
        data class Content(
            val newHashedPwd: String
        )
    }
}
