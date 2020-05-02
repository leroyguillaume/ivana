package io.ivana.api.impl

import java.util.*

internal data class SubjectPermissionsData(
    val subjectId: UUID,
    val permissions: Set<PermissionData>
)
