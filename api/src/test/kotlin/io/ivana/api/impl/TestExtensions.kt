package io.ivana.api.impl

import org.springframework.jdbc.core.JdbcTemplate

@Suppress("SqlWithoutWhere")
fun cleanDb(jdbc: JdbcTemplate) {
    jdbc.update("DELETE FROM ${UserEventRepositoryImpl.TableName}")
    jdbc.update("DELETE FROM ${UserRepositoryImpl.TableName}")
}
