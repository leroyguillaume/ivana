package io.ivana.dto

import java.time.OffsetDateTime
import java.util.*

data class AlbumDto(
    val id: UUID,
    val name: String,
    val creationDate: OffsetDateTime
) : EntityDto
