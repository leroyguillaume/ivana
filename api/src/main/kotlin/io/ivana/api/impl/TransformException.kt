package io.ivana.api.impl

class TransformException(
    override val message: String,
    override val cause: Throwable
) : RuntimeException()
