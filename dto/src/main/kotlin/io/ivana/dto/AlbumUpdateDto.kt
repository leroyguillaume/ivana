package io.ivana.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*
import javax.validation.constraints.Size

data class AlbumUpdateDto(
    @get:Size(min = AlbumNameMinSize, max = AlbumNameMaxSize)
    @JsonDeserialize(using = JacksonWhiteSpaceRemovalDeserializer::class)
    val name: String,

    val photosToAdd: List<UUID> = emptyList(),

    val photosToRemove: List<UUID> = emptyList()
)
