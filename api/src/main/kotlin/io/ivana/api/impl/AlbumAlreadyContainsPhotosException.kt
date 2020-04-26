package io.ivana.api.impl

import java.util.*

class AlbumAlreadyContainsPhotosException(
    val photosIds: Set<UUID>
) : RuntimeException()
