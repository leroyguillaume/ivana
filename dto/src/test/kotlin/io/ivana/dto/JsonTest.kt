package io.ivana.dto

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileNotFoundException

abstract class JsonTest(
    filename: String,
    private val expectedValue: Any,
    private val deserializeAs: TypeReference<*>,
    private val mapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule())
        .registerModule(JavaTimeModule())
) {
    private val filepath = "/json/$filename"
    private val file = javaClass.getResource(filepath)?.file?.let { File(it) }
        ?: throw FileNotFoundException("classpath:$filepath")

    @Test
    fun deserialization() {
        val deserialized = mapper.readValue(file, deserializeAs)
        deserialized shouldBe expectedValue
    }

    @Test
    fun serialization() {
        val json = mapper.writeValueAsString(expectedValue)
        val deserialized = mapper.readValue(json, deserializeAs)
        deserialized shouldBe expectedValue
    }
}

inline fun <reified T> typeOf() = object : TypeReference<T>() {}
