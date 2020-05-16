package io.ivana.dto

data class UpdatePermissionsDto(
    val permissionsToAdd: Set<SubjectPermissionsUpdateDto> = emptySet(),
    val permissionsToRemove: Set<SubjectPermissionsUpdateDto> = emptySet()
)
