package io.ivana.api.security

data class Jwt(
    val value: String,
    val expirationInSeconds: Int
)
