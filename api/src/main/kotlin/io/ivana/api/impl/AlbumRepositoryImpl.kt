package io.ivana.api.impl

import io.ivana.core.Album
import io.ivana.core.AlbumRepository
import io.ivana.core.Permission
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.*

@Repository
class AlbumRepositoryImpl(
    override val jdbc: NamedParameterJdbcTemplate
) : AlbumRepository, AbstractEntityRepository<Album>() {
    internal companion object {
        const val TableName = "album"
        const val NameColumnName = "name"
        const val OwnerIdColumnName = "owner_id"
        const val CreationDateColumnName = "creation_date"
    }

    override val tableName = TableName

    override fun count(ownerId: UUID) = jdbc.queryForObject(
        """
        SELECT COUNT($IdColumnName)
        FROM $tableName
        WHERE $OwnerIdColumnName = :owner_id
        """,
        MapSqlParameterSource(mapOf("owner_id" to ownerId))
    ) { rs, _ -> rs.getInt(1) }

    override fun countShared(userId: UUID) = jdbc.queryForObject(
        """
        SELECT COUNT(a.$IdColumnName)
        FROM $tableName a
        JOIN ${UserAlbumAuthorizationRepositoryImpl.TableName} uaa
        ON uaa.${UserAlbumAuthorizationRepositoryImpl.AlbumIdColumnName} = a.$IdColumnName
        WHERE a.$OwnerIdColumnName != :user_id 
            AND uaa.${UserAlbumAuthorizationRepositoryImpl.UserIdColumnName} = :user_id
            AND uaa.${AbstractAuthorizationRepository.CanReadColumnName} IS TRUE
        """,
        MapSqlParameterSource(mapOf("user_id" to userId))
    ) { rs, _ -> rs.getInt(1) }

    override fun fetchAll(ownerId: UUID, offset: Int, limit: Int) = jdbc.query(
        """
        SELECT *
        FROM $tableName
        WHERE $OwnerIdColumnName = :owner_id
        ORDER BY $CreationDateColumnName, $NameColumnName, $IdColumnName
        OFFSET :offset
        LIMIT :limit
        """,
        MapSqlParameterSource(mapOf("owner_id" to ownerId, "offset" to offset, "limit" to limit))
    ) { rs, _ -> rs.toEntity() }

    override fun fetchDuplicateIds(id: UUID, photosIds: Set<UUID>) = jdbc.query(
        """
        SELECT ${PhotoRepositoryImpl.PhotoIdColumnName}
        FROM ${PhotoRepositoryImpl.AlbumPhotoTableName}
        WHERE ${PhotoRepositoryImpl.AlbumIdColumnName} = :album_id 
            AND ${PhotoRepositoryImpl.PhotoIdColumnName} IN (:photos_ids)
        """,
        MapSqlParameterSource(mapOf("album_id" to id, "photos_ids" to photosIds))
    ) { rs, _ -> rs.getObject(1, UUID::class.java) }.toSet()

    override fun fetchOrder(id: UUID, photoId: UUID) = try {
        jdbc.queryForObject(
            """
        SELECT ${PhotoRepositoryImpl.OrderColumnName}
        FROM ${PhotoRepositoryImpl.AlbumPhotoTableName}
        WHERE ${PhotoRepositoryImpl.AlbumIdColumnName} = :album_id 
            AND ${PhotoRepositoryImpl.PhotoIdColumnName} = :photo_id 
        """,
            MapSqlParameterSource(mapOf("album_id" to id, "photo_id" to photoId))
        ) { rs, _ -> rs.getInt(1) }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun fetchShared(userId: UUID, offset: Int, limit: Int) = jdbc.query(
        """
        SELECT a.*
        FROM $tableName a
        JOIN ${UserAlbumAuthorizationRepositoryImpl.TableName} uaa
        ON uaa.${UserAlbumAuthorizationRepositoryImpl.AlbumIdColumnName} = a.$IdColumnName
        WHERE a.$OwnerIdColumnName != :user_id 
            AND uaa.${UserAlbumAuthorizationRepositoryImpl.UserIdColumnName} = :user_id
            AND uaa.${AbstractAuthorizationRepository.CanReadColumnName} IS TRUE
        ORDER BY $CreationDateColumnName, $NameColumnName, $IdColumnName
        OFFSET :offset
        LIMIT :limit
        """,
        MapSqlParameterSource(mapOf("user_id" to userId, "offset" to offset, "limit" to limit))
    ) { rs, _ -> rs.toEntity() }

    override fun fetchSize(id: UUID, userId: UUID) = jdbc.queryForObject(
        """
        SELECT COUNT(ap.${PhotoRepositoryImpl.PhotoIdColumnName})
        FROM ${PhotoRepositoryImpl.AlbumPhotoTableName} ap
        WHERE ap.${PhotoRepositoryImpl.AlbumIdColumnName} = :album_id
        AND (
            SELECT ${AbstractAuthorizationRepository.CanReadColumnName}
            FROM ${UserPhotoAuthorizationRepositoryImpl.TableName}
            WHERE ${UserPhotoAuthorizationRepositoryImpl.PhotoIdColumnName} = ap.${PhotoRepositoryImpl.PhotoIdColumnName}
                AND ${UserPhotoAuthorizationRepositoryImpl.UserIdColumnName} = :user_id
        ) IS NOT FALSE
        """,
        MapSqlParameterSource(mapOf("album_id" to id, "user_id" to userId))
    ) { rs, _ -> rs.getInt(1) }

    override fun suggest(name: String, count: Int, userId: UUID, perm: Permission): List<Album> {
        val permColumnName = when (perm) {
            Permission.Read -> AbstractAuthorizationRepository.CanReadColumnName
            Permission.Update -> AbstractAuthorizationRepository.CanUpdateColumnName
            Permission.Delete -> AbstractAuthorizationRepository.CanDeleteColumnName
            Permission.UpdatePermissions -> AbstractAuthorizationRepository.CanUpdatePermissionsColumnName
        }
        return jdbc.query(
            """
        SELECT a.*
        FROM $TableName a
        JOIN ${UserAlbumAuthorizationRepositoryImpl.TableName} uaa 
        ON uaa.${UserAlbumAuthorizationRepositoryImpl.AlbumIdColumnName} = a.$IdColumnName
        WHERE uaa.${UserAlbumAuthorizationRepositoryImpl.UserIdColumnName} = :user_id 
            AND uaa.$permColumnName IS TRUE
            AND LOWER(a.$NameColumnName) LIKE LOWER('%' || :name || '%')
        ORDER BY a.$NameColumnName
        LIMIT :limit
        """,
            MapSqlParameterSource(mapOf("user_id" to userId, "name" to name, "limit" to count))
        ) { rs, _ -> rs.toEntity() }
    }

    override fun ResultSet.toEntity() = Album(
        id = getObject(IdColumnName, UUID::class.java),
        ownerId = getObject(OwnerIdColumnName, UUID::class.java),
        name = getString(NameColumnName),
        creationDate = getObject(CreationDateColumnName, OffsetDateTime::class.java)
    )
}
