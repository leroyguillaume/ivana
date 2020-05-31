package io.ivana.api.impl

import io.ivana.core.UserPhotoAuthorizationRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserPhotoAuthorizationRepositoryImpl(
    jdbc: NamedParameterJdbcTemplate
) : UserPhotoAuthorizationRepository, AbstractAuthorizationRepository(jdbc) {
    internal companion object {
        const val TableName = "user_photo_authorization"

        const val UserIdColumnName = "user_id"
        const val PhotoIdColumnName = "photo_id"
    }

    override val tableName = TableName
    override val subjectIdColumnName = UserIdColumnName
    override val resourceIdColumnName = PhotoIdColumnName

    override fun photoIsInReadableAlbum(photoId: UUID, userId: UUID) = try {
        jdbc.queryForObject(
            """
            SELECT uaa.$CanReadColumnName
            FROM ${UserAlbumAuthorizationRepositoryImpl.TableName} uaa
            JOIN ${AlbumRepositoryImpl.TableName} a
            ON a.${AbstractEntityRepository.IdColumnName} = uaa.${UserAlbumAuthorizationRepositoryImpl.AlbumIdColumnName}
            JOIN ${PhotoRepositoryImpl.AlbumPhotoTableName} ap
            ON ap.${PhotoRepositoryImpl.AlbumIdColumnName} = a.${AbstractEntityRepository.IdColumnName}
            WHERE ap.${PhotoRepositoryImpl.PhotoIdColumnName} = :photo_id 
                AND uaa.${UserAlbumAuthorizationRepositoryImpl.UserIdColumnName} = :user_id
        """,
            MapSqlParameterSource(mapOf("photo_id" to photoId, "user_id" to userId))
        ) { rs, _ -> rs.getBoolean(1) }
    } catch (exception: EmptyResultDataAccessException) {
        false
    }

    override fun userCanReadAll(photosIds: Set<UUID>, userId: UUID) = try {
        jdbc.queryForObject(
            """
            SELECT bool_and(user_can_read(${AbstractEntityRepository.IdColumnName}, :user_id))
            FROM ${PhotoRepositoryImpl.TableName}
            WHERE ${AbstractEntityRepository.IdColumnName} IN (:photos_ids)
            """,
            MapSqlParameterSource(mapOf("user_id" to userId, "photos_ids" to photosIds))
        ) { rs, _ -> rs.getBoolean(1) }
    } catch (exception: EmptyResultDataAccessException) {
        false
    }
}
