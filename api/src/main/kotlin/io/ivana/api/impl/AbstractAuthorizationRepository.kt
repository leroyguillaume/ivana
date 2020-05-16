package io.ivana.api.impl

import io.ivana.core.AuthorizationRepository
import io.ivana.core.Permission
import io.ivana.core.SubjectPermissions
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*

abstract class AbstractAuthorizationRepository(
    protected val jdbc: NamedParameterJdbcTemplate
) : AuthorizationRepository {
    internal companion object {
        const val CanReadColumnName = "can_read"
        const val CanUpdateColumnName = "can_update"
        const val CanDeleteColumnName = "can_delete"
        const val CanUpdatePermissionsColumnName = "can_update_permissions"

        val ColumnToPermission = mapOf(
            CanReadColumnName to Permission.Read,
            CanUpdateColumnName to Permission.Update,
            CanDeleteColumnName to Permission.Delete,
            CanUpdatePermissionsColumnName to Permission.UpdatePermissions
        )
    }

    protected abstract val tableName: String
    protected abstract val subjectIdColumnName: String
    protected abstract val resourceIdColumnName: String

    override fun count(resourceId: UUID) = jdbc.queryForObject(
        """
        SELECT COUNT($subjectIdColumnName)
        FROM $tableName
        WHERE $resourceIdColumnName = :resource_id
        """.trimIndent(),
        MapSqlParameterSource(mapOf("resource_id" to resourceId))
    ) { rs, _ -> rs.getInt(1) }

    override fun fetch(subjectId: UUID, resourceId: UUID): Set<Permission>? = try {
        jdbc.queryForObject(
            """
            SELECT *
            FROM $tableName
            WHERE $subjectIdColumnName = :subject_id AND $resourceIdColumnName = :resource_id
            """,
            MapSqlParameterSource(mapOf("subject_id" to subjectId, "resource_id" to resourceId))
        ) { rs, _ -> rs.toPermissions() }!!
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun fetchAll(resourceId: UUID, offset: Int, limit: Int) = jdbc.query(
        """
        SELECT *
        FROM $tableName
        WHERE $resourceIdColumnName = :resource_id
        ORDER BY $subjectIdColumnName
        OFFSET :offset
        LIMIT :limit
        """.trimIndent(),
        MapSqlParameterSource(mapOf("resource_id" to resourceId, "offset" to offset, "limit" to limit))
    ) { rs, _ -> rs.toSubjectPermissions() }

    protected fun ResultSet.toPermissions() = ColumnToPermission
        .map { entry ->
            if (getBoolean(entry.key)) {
                setOf(entry.value)
            } else {
                emptySet()
            }
        }
        .flatten()
        .toSet()

    private fun ResultSet.toSubjectPermissions() = SubjectPermissions(
        subjectId = getObject(subjectIdColumnName, UUID::class.java),
        permissions = toPermissions()
    )
}
