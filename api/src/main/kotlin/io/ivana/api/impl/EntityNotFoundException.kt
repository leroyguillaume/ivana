package io.ivana.api.impl

class EntityNotFoundException(
    override val message: String
) : RuntimeException()
