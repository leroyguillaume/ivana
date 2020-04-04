package io.ivana.dto

data class PageDto<E : EntityDto>(
    val content: List<E>,
    val no: Int,
    val totalPages: Int,
    val totalItems: Int
)
