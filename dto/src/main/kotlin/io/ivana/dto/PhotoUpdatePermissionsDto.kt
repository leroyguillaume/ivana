package io.ivana.dto

data class PhotoUpdatePermissionsDto(
    val permissionsToAdd: Set<SubjectPermissionsUpdateDto> = emptySet(),
    val permissionsToRemove: Set<SubjectPermissionsUpdateDto> = emptySet()
)
