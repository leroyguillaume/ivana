package io.ivana.core

import java.time.OffsetDateTime
import java.util.*

sealed class PhotoEvent : Event {
    data class Upload(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val source: EventSource.User,
        val content: Content
    ) : PhotoEvent() {
        data class Content(
            val type: Photo.Type,
            val hash: String
        )

        override val number = 1L
    }
}
