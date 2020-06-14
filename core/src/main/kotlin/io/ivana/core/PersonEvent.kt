package io.ivana.core

import java.time.OffsetDateTime
import java.util.*

sealed class PersonEvent : Event {
    data class Creation(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource.User,
        val content: Content
    ) : PersonEvent() {
        data class Content(
            val lastName: String,
            val firstName: String
        )
    }

    data class Deletion(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource.User
    ) : PersonEvent()

    data class Update(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource.User,
        val content: Content
    ) : PersonEvent() {
        data class Content(
            val lastName: String,
            val firstName: String
        )
    }
}
