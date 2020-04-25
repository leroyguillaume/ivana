package io.ivana.api.impl

import java.util.*

internal sealed class AlbumEventData : EventData {
    data class Creation(
        override val source: EventSourceData.User,
        val content: Content
    ) : AlbumEventData() {
        data class Content(
            val name: String
        )
    }

    data class Update(
        override val source: EventSourceData.User,
        val content: Content
    ) : AlbumEventData() {
        data class Content(
            val name: String,
            val photosToAdd: List<UUID>,
            val photosToRemove: List<UUID>
        )
    }
}