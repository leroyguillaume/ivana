package io.ivana.api.impl

import io.ivana.core.Entity
import io.ivana.core.EntityRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.ResultSet
import java.util.*

abstract class AbstractEntityRepository<E : Entity>(
    protected val jdbc: JdbcTemplate
) : EntityRepository<E> {
    internal companion object {
        const val IdColumnName = "id"
    }

    protected abstract val tableName: String

    override fun fetchById(id: UUID) = fetchBy(IdColumnName, id)

    protected fun fetchBy(columnName: String, value: Any) = try {
        jdbc.queryForObject(
            """
        SELECT *
        FROM $tableName
        WHERE $columnName = ?
        """,
            arrayOf(value)
        ) { rs, _ -> entityFromResultSet(rs) }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    protected abstract fun entityFromResultSet(rs: ResultSet): E
}
