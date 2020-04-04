package io.ivana.core

data class LinkedPhotos(
    val current: Photo,
    val previous: Photo? = null,
    val next: Photo? = null
)
