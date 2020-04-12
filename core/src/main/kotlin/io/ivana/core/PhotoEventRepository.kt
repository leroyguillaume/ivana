package io.ivana.core

import java.util.*

interface PhotoEventRepository : EventRepository<PhotoEvent> {
    fun saveTransformEvent(photoId: UUID, transform: Transform, source: EventSource.User): PhotoEvent.Transform

    fun saveUploadEvent(content: PhotoEvent.Upload.Content, source: EventSource.User): PhotoEvent.Upload
}
