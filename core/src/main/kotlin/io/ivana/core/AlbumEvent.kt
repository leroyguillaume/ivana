package io.ivana.core

import java.time.OffsetDateTime
import java.util.*

sealed class AlbumEvent : Event {
    data class Creation(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val source: EventSource.User,
        val albumName: String
    ) : AlbumEvent() {
        override val number = 1L
    }
}
