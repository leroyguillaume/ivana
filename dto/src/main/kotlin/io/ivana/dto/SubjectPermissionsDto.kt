package io.ivana.dto

import java.util.*

data class SubjectPermissionsDto(
    val subjectId: UUID,
    val subjectName: String,
    val permissions: Set<PermissionDto>
)
