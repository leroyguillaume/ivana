package io.ivana.api.impl

import java.util.*

class PhotosNotFoundException(
    val photosIds: Set<UUID>
) : RuntimeException()
