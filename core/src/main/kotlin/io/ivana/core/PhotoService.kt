package io.ivana.core

import java.io.File
import java.io.InputStream
import java.util.*

interface PhotoService : EntityService<Photo> {
    fun getAll(ownerId: UUID, pageNo: Int, pageSize: Int): Page<Photo>

    fun getCompressedFile(photo: Photo): File

    fun getRawFile(photo: Photo): File

    fun getTimeWindowById(id: UUID): PhotosTimeWindow

    fun uploadPhoto(input: InputStream, type: Photo.Type, source: EventSource.User): Photo
}
