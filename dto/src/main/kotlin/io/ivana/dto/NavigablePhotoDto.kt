package io.ivana.dto

import java.net.URI
import java.util.*

data class NavigablePhotoDto(
    val id: UUID,
    val rawUri: URI,
    val compressedUri: URI,
    val previous: PhotoDto? = null,
    val next: PhotoDto? = null
)
