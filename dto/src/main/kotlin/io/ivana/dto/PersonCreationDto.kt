package io.ivana.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import javax.validation.constraints.Size

const val PersonFirstNameMinSize = 1
const val PersonFirstNameMaxSize = 50
const val PersonLastNameMinSize = 1
const val PersonLastNameMaxSize = 50

data class PersonCreationDto(
    @get:Size(min = PersonLastNameMinSize, max = PersonLastNameMaxSize)
    @JsonDeserialize(using = JacksonWhiteSpaceRemovalDeserializer::class)
    val lastName: String,

    @get:Size(min = PersonFirstNameMinSize, max = PersonFirstNameMaxSize)
    @JsonDeserialize(using = JacksonWhiteSpaceRemovalDeserializer::class)
    val firstName: String
)
