package io.ivana.api.security

class BadJwtException(
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException()
