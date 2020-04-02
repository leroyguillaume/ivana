@file:Suppress("ClassName")

package io.ivana.api.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

@Suppress("SpringJavaAutowiredMembersInspection")
internal abstract class AbstractAuthorizationRepositoryTest {
    protected abstract val tableName: String
    protected abstract val subjectIdColumnName: String
    protected abstract val resourceIdColumnName: String

    @Autowired
    protected lateinit var jdbc: NamedParameterJdbcTemplate

    protected fun updateAuthorization(subjectId: UUID, resourceId: UUID, vararg permissions: Permission) {
        val canRead = permissions.contains(Permission.Read)
        val canUpdate = permissions.contains(Permission.Update)
        val canDelete = permissions.contains(Permission.Delete)
        jdbc.update(
            """
            UPDATE $tableName
            SET ${AbstractAuthorizationRepository.CanReadColumnName} = :can_read,
                ${AbstractAuthorizationRepository.CanUpdateColumnName} = :can_update,
                ${AbstractAuthorizationRepository.CanDeleteColumnName} = :can_delete
            WHERE $subjectIdColumnName = :subject_id AND $resourceIdColumnName = :resource_id
            """,
            MapSqlParameterSource(
                mapOf(
                    "can_read" to canRead,
                    "can_update" to canUpdate,
                    "can_delete" to canDelete,
                    "subject_id" to subjectId,
                    "resource_id" to resourceId
                )
            )
        )
    }
}
