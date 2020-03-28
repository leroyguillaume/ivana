package io.ivana.api.impl

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.jdbc.core.JdbcTemplate

@Suppress("SqlWithoutWhere")
fun cleanDb(jdbc: JdbcTemplate) {
    jdbc.update("DELETE FROM ${UserEventRepositoryImpl.TableName}")
    jdbc.update("DELETE FROM ${UserRepositoryImpl.TableName}")
}

fun jdbcTemplate() = JdbcTemplate(
    DataSourceBuilder.create()
        .url(System.getProperty("database.url"))
        .username(System.getProperty("database.username"))
        .password(System.getProperty("database.password"))
        .build()
)
