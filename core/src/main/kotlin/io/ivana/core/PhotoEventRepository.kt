package io.ivana.core

import java.util.*

interface PhotoEventRepository : EventRepository<PhotoEvent> {
    fun saveDeletionEvent(photoId: UUID, source: EventSource.User): PhotoEvent.Deletion

    fun saveTransformEvent(photoId: UUID, transform: Transform, source: EventSource.User): PhotoEvent.Transform

    fun saveUpdatePermissionsEvent(
        photoId: UUID,
        content: PhotoEvent.UpdatePermissions.Content,
        source: EventSource.User
    ): PhotoEvent.UpdatePermissions

    fun saveUploadEvent(content: PhotoEvent.Upload.Content, source: EventSource.User): PhotoEvent.Upload
}
