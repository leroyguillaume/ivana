package io.ivana.dto

import java.net.URI
import java.util.*

sealed class PhotoDto : EntityDto {
    sealed class Complete : PhotoDto() {
        data class Navigable(
            override val id: UUID,
            override val ownerId: UUID,
            override val rawUri: URI,
            override val compressedUri: URI,
            override val permissions: Set<PermissionDto>,
            val previous: Light? = null,
            val next: Light? = null
        ) : Complete()

        data class Simple(
            override val id: UUID,
            override val ownerId: UUID,
            override val rawUri: URI,
            override val compressedUri: URI,
            override val permissions: Set<PermissionDto>
        ) : Complete()

        abstract val permissions: Set<PermissionDto>
    }

    data class Light(
        override val id: UUID,
        override val ownerId: UUID,
        override val rawUri: URI,
        override val compressedUri: URI
    ) : PhotoDto()

    abstract val id: UUID
    abstract val ownerId: UUID
    abstract val rawUri: URI
    abstract val compressedUri: URI
}
