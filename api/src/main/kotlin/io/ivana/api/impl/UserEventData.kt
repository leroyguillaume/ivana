package io.ivana.api.impl

internal sealed class UserEventData : EventData {
    data class Creation(
        override val source: EventSourceData,
        val content: Content
    ) : UserEventData() {
        data class Content(
            val name: String,
            val hashedPwd: String
        )
    }

    data class Login(
        override val source: EventSourceData.User
    ) : UserEventData()
}
