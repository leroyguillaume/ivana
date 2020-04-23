package io.ivana.api.impl

internal sealed class AlbumEventData : EventData {
    data class Creation(
        override val source: EventSourceData.User,
        val content: Content
    ) : AlbumEventData() {
        data class Content(
            val name: String
        )
    }
}
