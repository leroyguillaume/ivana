package io.ivana.api.impl

import io.ivana.core.Photo
import io.ivana.core.PhotoEvent
import io.ivana.core.User
import io.ivana.core.UserEvent
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

fun PhotoEvent.Upload.toPhoto(no: Int, version: Int = 1) = Photo(
    id = subjectId,
    ownerId = source.id,
    uploadDate = date,
    type = content.type,
    hash = content.hash,
    no = no,
    version = version
)

fun UserEvent.Creation.toUser() = User(
    id = subjectId,
    name = content.name,
    hashedPwd = content.hashedPwd,
    role = content.role,
    creationDate = date
)
