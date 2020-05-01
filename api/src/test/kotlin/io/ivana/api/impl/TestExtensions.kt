package io.ivana.api.impl

import io.ivana.core.Photo
import io.ivana.core.PhotoEvent
import io.ivana.core.User
import io.ivana.core.UserEvent
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Suppress("SqlWithoutWhere")
internal fun cleanDb(jdbc: NamedParameterJdbcTemplate) {
    jdbc.deleteAllOfTables(
        UserEventRepositoryImpl.TableName,
        PhotoEventRepositoryImpl.TableName,
        AlbumEventRepositoryImpl.TableName,
        UserRepositoryImpl.TableName
    )
    jdbc.resetEventNumberSequence(
        UserEventRepositoryImpl.TableName,
        PhotoEventRepositoryImpl.TableName,
        AlbumEventRepositoryImpl.TableName
    )
    jdbc.update(
        "ALTER SEQUENCE ${PhotoRepositoryImpl.TableName}_${PhotoRepositoryImpl.NoColumnName}_seq RESTART",
        MapSqlParameterSource()
    )
}

internal fun PhotoEvent.Upload.toPhoto(no: Int, version: Int = 1) = Photo(
    id = subjectId,
    ownerId = source.id,
    uploadDate = date,
    type = content.type,
    hash = content.hash,
    no = no,
    version = version
)

internal fun UserEvent.Creation.toUser() = User(
    id = subjectId,
    name = content.name,
    hashedPwd = content.hashedPwd,
    role = content.role,
    creationDate = date
)

@Suppress("SqlWithoutWhere")
private fun NamedParameterJdbcTemplate.deleteAllOfTables(vararg tableNames: String) {
    tableNames.forEach { update("DELETE FROM $it", MapSqlParameterSource()) }
}

@Suppress("SqlResolve")
private fun NamedParameterJdbcTemplate.resetEventNumberSequence(vararg tableNames: String) {
    tableNames.forEach { tableName ->
        update(
            "ALTER SEQUENCE ${tableName}_${AbstractEventRepository.NumberColumnName}_seq RESTART",
            MapSqlParameterSource()
        )
    }
}
