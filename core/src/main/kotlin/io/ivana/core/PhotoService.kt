package io.ivana.core

import java.io.File
import java.io.InputStream
import java.util.*

interface PhotoService : OwnableEntityService<Photo> {
    fun delete(id: UUID, source: EventSource.User)

    fun getCompressedFile(photo: Photo): File

    fun getLinkedById(id: UUID): LinkedPhotos

    fun getRawFile(photo: Photo): File

    fun transform(id: UUID, transform: Transform, source: EventSource.User)

    fun uploadPhoto(input: InputStream, type: Photo.Type, source: EventSource.User): Photo
}
