package io.ivana.core

import java.io.InputStream
import java.util.*

interface PhotoService : EntityService<Photo> {
    fun getTimeWindowById(id: UUID): PhotosTimeWindow

    fun uploadPhoto(input: InputStream, type: Photo.Type, source: EventSource.User): Photo
}
