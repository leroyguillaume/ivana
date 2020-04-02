package io.ivana.api.security

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*

abstract class AbstractAuthorizationRepository(
    protected val jdbc: NamedParameterJdbcTemplate
) {
    internal companion object {
        const val CanReadColumnName = "can_read"
        const val CanUpdateColumnName = "can_update"
        const val CanDeleteColumnName = "can_delete"

        val ColumnToPermission = mapOf(
            CanReadColumnName to Permission.Read,
            CanUpdateColumnName to Permission.Update,
            CanDeleteColumnName to Permission.Delete
        )
    }

    protected abstract val tableName: String
    protected abstract val subjectIdColumnName: String
    protected abstract val resourceIdColumnName: String

    open fun fetch(subjectId: UUID, resourceId: UUID): Set<Permission> = try {
        jdbc.queryForObject(
            """
        SELECT *
        FROM $tableName
        WHERE $subjectIdColumnName = :$subjectIdColumnName AND $resourceIdColumnName = :$resourceIdColumnName
        """,
            MapSqlParameterSource(mapOf(subjectIdColumnName to subjectId, resourceIdColumnName to resourceId))
        ) { rs, _ -> rs.toPermissions() }!!
    } catch (exception: EmptyResultDataAccessException) {
        emptySet()
    }

    private fun ResultSet.toPermissions() = arrayOf(CanReadColumnName, CanUpdateColumnName, CanDeleteColumnName)
        .map { columnName ->
            if (getBoolean(columnName)) {
                setOf(ColumnToPermission.getValue(columnName))
            } else {
                emptySet()
            }
        }
        .flatten()
        .toSet()
}
