package io.ivana.dto

import java.net.URI
import java.util.*

sealed class PhotoDto : EntityDto {
    data class Simple(
        override val id: UUID,
        override val rawUri: URI,
        override val compressedUri: URI
    ) : PhotoDto()

    data class Navigable(
        override val id: UUID,
        override val rawUri: URI,
        override val compressedUri: URI,
        val previous: Simple? = null,
        val next: Simple? = null
    ) : PhotoDto()

    abstract val id: UUID
    abstract val rawUri: URI
    abstract val compressedUri: URI
}
