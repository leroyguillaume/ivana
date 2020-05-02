package io.ivana.core

data class Page<E>(
    val content: List<E>,
    val no: Int,
    val totalPages: Int,
    val totalItems: Int
)
