package io.ivana.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import javax.validation.constraints.Size

const val UserNameMinSize = 3
const val UserNameMaxSize = 50
const val UserPasswordMinSize = 8

data class UserCreationDto(
    @get:Size(min = UserNameMinSize, max = UserNameMaxSize)
    @JsonDeserialize(using = JacksonWhiteSpaceRemovalDeserializer::class)
    val name: String,

    @get:Size(min = UserPasswordMinSize)
    val pwd: String,

    val role: RoleDto
)
