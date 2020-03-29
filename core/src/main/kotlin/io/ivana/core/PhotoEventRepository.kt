package io.ivana.core

interface PhotoEventRepository : EventRepository<PhotoEvent> {
    fun saveUploadEvent(content: PhotoEvent.Upload.Content, source: EventSource.User): PhotoEvent.Upload
}
