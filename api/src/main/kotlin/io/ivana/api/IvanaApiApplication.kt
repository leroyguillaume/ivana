package io.ivana.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.Clock


@SpringBootApplication
@ConfigurationPropertiesScan(value = ["io.ivana.api.config"])
class IvanaApiApplication {
    @Bean
    fun clock() = Clock.systemDefaultZone()
}

fun main(args: Array<String>) {
    runApplication<IvanaApiApplication>(*args)
}
