package io.ivana.api.impl

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Suppress("SqlWithoutWhere")
internal fun cleanDb(jdbc: NamedParameterJdbcTemplate) {
    jdbc.apply {
        update("DELETE FROM ${UserEventRepositoryImpl.TableName}", MapSqlParameterSource())
        update("DELETE FROM ${UserRepositoryImpl.TableName}", MapSqlParameterSource())
        update(
            "ALTER SEQUENCE ${PhotoRepositoryImpl.TableName}_${PhotoRepositoryImpl.NoColumnName}_seq RESTART",
            MapSqlParameterSource()
        )
    }
}
