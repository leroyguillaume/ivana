package io.ivana.api.impl

import io.ivana.core.Entity
import io.ivana.core.EntityRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*

abstract class AbstractEntityRepository<E : Entity> : EntityRepository<E> {
    internal companion object {
        const val IdColumnName = "id"
    }

    protected abstract val jdbc: NamedParameterJdbcTemplate

    protected abstract val tableName: String

    override fun count() = jdbc.queryForObject(
        """
        SELECT COUNT($IdColumnName)
        FROM $tableName
        """,
        MapSqlParameterSource()
    ) { rs, _ -> rs.getInt(1) }

    override fun existsById(id: UUID) = jdbc.queryForObject(
        """
        SELECT EXISTS(
            SELECT 1
            FROM $tableName
            WHERE $IdColumnName = :id
        )
        """,
        MapSqlParameterSource(mapOf("id" to id))
    ) { rs, _ -> rs.getBoolean(1) }

    override fun fetchAll(offset: Int, limit: Int) = jdbc.query(
        """
        SELECT *
        FROM $tableName
        ORDER BY $IdColumnName
        OFFSET :offset
        LIMIT :limit
        """,
        MapSqlParameterSource(mapOf("offset" to offset, "limit" to limit))
    ) { rs, _ -> rs.toEntity() }

    override fun fetchAllByIds(ids: Set<UUID>) = if (ids.isEmpty()) {
        emptySet()
    } else {
        jdbc.query(
            """
            SELECT *
            FROM $tableName
            WHERE $IdColumnName IN (:ids)
            """,
            MapSqlParameterSource(mapOf("ids" to ids))
        ) { rs, _ -> rs.toEntity() }.toSet()
    }

    override fun fetchById(id: UUID) = fetchBy(IdColumnName, id)

    override fun fetchExistingIds(ids: Set<UUID>) = jdbc.query(
        """
        SELECT $IdColumnName
        FROM $tableName
        WHERE $IdColumnName IN (:ids)
        """,
        MapSqlParameterSource(mapOf("ids" to ids))
    ) { rs, _ -> rs.getObject(1, UUID::class.java) }.toSet()

    protected fun fetchBy(columnName: String, value: Any) = fetchBy(Column(columnName, value))

    protected fun fetchBy(vararg columns: Column) = try {
        val whereSql = columns
            .map { "${it.name} = :${it.name}" }
            .reduce { acc, sql -> "$acc AND $sql" }
        val params = MapSqlParameterSource(columns.map { it.name to it.value }.toMap())
        jdbc.queryForObject(
            """
            SELECT *
            FROM $tableName
            WHERE $whereSql
            """,
            params
        ) { rs, _ -> rs.toEntity() }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    protected abstract fun ResultSet.toEntity(): E

    protected data class Column(
        val name: String,
        val value: Any
    )
}
