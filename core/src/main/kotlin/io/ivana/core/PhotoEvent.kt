package io.ivana.core

import java.time.OffsetDateTime
import java.util.*

sealed class PhotoEvent : Event {
    data class Deletion(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource.User
    ) : PhotoEvent()

    data class Transform(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource.User,
        val transform: io.ivana.core.Transform
    ) : PhotoEvent()

    data class Upload(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource.User,
        val content: Content
    ) : PhotoEvent() {
        data class Content(
            val type: Photo.Type,
            val hash: String
        )
    }
}
