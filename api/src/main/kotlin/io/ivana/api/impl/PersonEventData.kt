package io.ivana.api.impl

internal sealed class PersonEventData : EventData {
    data class Creation(
        override val source: EventSourceData.User,
        val content: Content
    ) : PersonEventData() {
        data class Content(
            val lastName: String,
            val firstName: String
        )
    }

    data class Deletion(
        override val source: EventSourceData.User
    ) : PersonEventData()

    data class Update(
        override val source: EventSourceData.User,
        val content: Content
    ) : PersonEventData() {
        data class Content(
            val lastName: String,
            val firstName: String
        )
    }
}
