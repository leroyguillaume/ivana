package io.ivana.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.io.File

@ConstructorBinding
@ConfigurationProperties(prefix = "ivana")
data class IvanaProperties(
    val dataDir: File,
    val compressionQuality: Float
)
