package io.ivana.api.impl

import io.ivana.core.Entity
import io.ivana.core.EntityRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*

abstract class AbstractEntityRepository<E : Entity>(
    protected val jdbc: NamedParameterJdbcTemplate
) : EntityRepository<E> {
    internal companion object {
        const val IdColumnName = "id"
    }

    protected abstract val tableName: String

    override fun fetchById(id: UUID) = fetchBy(IdColumnName, id)

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
        ) { rs, _ -> entityFromResultSet(rs) }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    protected abstract fun entityFromResultSet(rs: ResultSet): E

    protected data class Column(
        val name: String,
        val value: Any
    )
}
