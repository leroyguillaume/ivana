package io.ivana.dto

import java.util.*

data class SubjectPermissionsUpdateDto(
    val subjectId: UUID,
    val permissions: Set<PermissionDto>
)
