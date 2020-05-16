package io.ivana.dto

import java.time.OffsetDateTime
import java.util.*

sealed class AlbumDto : EntityDto {
    data class Complete(
        override val id: UUID,
        override val name: String,
        override val ownerId: UUID,
        override val creationDate: OffsetDateTime,
        val permissions: Set<PermissionDto>
    ) : AlbumDto()

    data class Light(
        override val id: UUID,
        override val name: String,
        override val ownerId: UUID,
        override val creationDate: OffsetDateTime
    ) : AlbumDto()

    abstract val id: UUID
    abstract val name: String
    abstract val ownerId: UUID
    abstract val creationDate: OffsetDateTime
}
