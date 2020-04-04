package io.ivana.core

data class Page<E : Entity>(
    val content: List<E>,
    val no: Int,
    val totalPages: Int,
    val totalItems: Int
)
