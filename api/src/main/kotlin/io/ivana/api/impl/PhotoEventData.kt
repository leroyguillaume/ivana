package io.ivana.api.impl

internal sealed class PhotoEventData : EventData {
    data class Upload(
        override val source: EventSourceData.User,
        val content: Content
    ) : PhotoEventData() {
        data class Content(
            val type: PhotoTypeData,
            val hash: String
        )
    }
}
