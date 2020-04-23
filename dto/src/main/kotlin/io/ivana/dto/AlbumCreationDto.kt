package io.ivana.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import javax.validation.constraints.Size

const val AlbumNameMinSize = 3
const val AlbumNameMaxSize = 50

data class AlbumCreationDto(
    @get:Size(min = AlbumNameMinSize, max = AlbumNameMaxSize)
    @JsonDeserialize(using = JacksonWhiteSpaceRemovalDeserializer::class)
    val name: String
)
