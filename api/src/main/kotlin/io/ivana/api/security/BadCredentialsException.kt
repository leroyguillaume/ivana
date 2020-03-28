package io.ivana.api.security

class BadCredentialsException(
    override val message: String
) : RuntimeException()
