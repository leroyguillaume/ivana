package io.ivana.core

import java.util.*

data class SubjectPermissions(
    val subjectId: UUID,
    val permissions: Set<Permission>
)
