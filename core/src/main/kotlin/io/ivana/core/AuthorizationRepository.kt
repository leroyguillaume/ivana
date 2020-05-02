package io.ivana.core

import java.util.*

interface AuthorizationRepository {
    fun count(resourceId: UUID): Int

    fun fetch(subjectId: UUID, resourceId: UUID): Set<Permission>

    fun fetchAll(resourceId: UUID, offset: Int, limit: Int): List<SubjectPermissions>
}
