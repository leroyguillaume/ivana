package io.ivana.core

import java.util.*

interface UserPhotoAuthorizationRepository : AuthorizationRepository {
    fun photoIsInReadableAlbum(photoId: UUID, userId: UUID): Boolean

    fun userCanReadAll(photosIds: Set<UUID>, userId: UUID): Boolean
}
