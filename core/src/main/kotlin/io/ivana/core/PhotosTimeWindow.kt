package io.ivana.core

data class PhotosTimeWindow(
    val current: Photo,
    val previous: Photo? = null,
    val next: Photo? = null
)
