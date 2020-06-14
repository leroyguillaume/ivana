package io.ivana.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import javax.validation.constraints.Size

data class PersonUpdateDto(
    @get:Size(min = PersonLastNameMinSize, max = PersonLastNameMaxSize)
    @JsonDeserialize(using = JacksonWhiteSpaceRemovalDeserializer::class)
    val lastName: String,

    @get:Size(min = PersonFirstNameMinSize, max = PersonFirstNameMaxSize)
    @JsonDeserialize(using = JacksonWhiteSpaceRemovalDeserializer::class)
    val firstName: String
)
