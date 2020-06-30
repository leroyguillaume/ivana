package io.ivana.dto

import java.util.*

data class PhotoPeopleUpdateDto(
    val peopleToAdd: Set<UUID> = emptySet(),
    val peopleToRemove: Set<UUID> = emptySet()
)
