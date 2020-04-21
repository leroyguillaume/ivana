package io.ivana.core

import java.time.OffsetDateTime
import java.util.*

sealed class UserEvent : Event {
    data class Creation(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val source: EventSource,
        val content: Content
    ) : UserEvent() {
        data class Content(
            val name: String,
            val hashedPwd: String,
            val role: Role
        )

        override val number = 1L
    }

    data class Deletion(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource
    ) : UserEvent()

    data class Login(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource.User
    ) : UserEvent()

    data class PasswordUpdate(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource,
        val newHashedPwd: String
    ) : UserEvent()
}
