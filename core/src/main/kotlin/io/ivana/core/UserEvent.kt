package io.ivana.core

import java.time.Instant
import java.util.*

sealed class UserEvent : Event {
    data class Creation(
        override val date: Instant,
        override val subjectId: UUID,
        override val source: EventSource,
        val content: Content
    ) : UserEvent() {
        data class Content(
            val name: String,
            val hashedPwd: String
        )

        override val number = 1L
    }

    data class Login(
        override val date: Instant,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource.User
    ) : UserEvent()
}
