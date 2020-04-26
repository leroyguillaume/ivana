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

    data class Deletion(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource.User
    ) : AlbumEvent()

    data class Update(
        override val date: OffsetDateTime,
        override val subjectId: UUID,
        override val number: Long,
        override val source: EventSource.User,
        val content: Content
    ) : AlbumEvent() {
        data class Content(
            val name: String,
            val photosToAdd: List<UUID>,
            val photosToRemove: List<UUID>
        )
    }
}
