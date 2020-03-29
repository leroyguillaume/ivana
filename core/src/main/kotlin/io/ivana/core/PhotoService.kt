package io.ivana.core

import java.io.InputStream

interface PhotoService {
    fun uploadPhoto(input: InputStream, type: Photo.Type, source: EventSource.User): Photo
}
