package io.ivana.api.impl

import io.ivana.core.Photo

class PhotoAlreadyUploadedException(
    val photo: Photo
) : RuntimeException()
