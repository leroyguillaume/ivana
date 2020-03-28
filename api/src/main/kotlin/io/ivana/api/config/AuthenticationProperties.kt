@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package io.ivana.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "auth")
data class AuthenticationProperties(
    val expirationInSeconds: Int = 7 * 24 * 60 * 60,
    val secret: String
)
