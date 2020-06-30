package io.ivana.api.impl

import java.util.*

class PhotosAlreadyInAlbumException(
    val photosIds: Set<UUID>
) : RuntimeException()
