package io.ivana.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.time.Clock

@SpringBootApplication
@ConfigurationPropertiesScan(value = ["io.ivana.api.config"])
@EnableWebMvc
class IvanaApiApplication {
    @Bean
    fun clock() = Clock.systemDefaultZone()
}

fun main(args: Array<String>) {
    runApplication<IvanaApiApplication>(*args)
}
