package io.ivana.dto

import java.net.URI
import java.util.*

data class PhotoDto(
    val id: UUID,
    val rawUri: URI,
    val compressedUri: URI
)
