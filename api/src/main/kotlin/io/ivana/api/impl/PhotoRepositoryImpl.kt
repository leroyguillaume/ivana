package io.ivana.api.impl

import io.ivana.core.Photo
import io.ivana.core.PhotoRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Repository
class PhotoRepositoryImpl(
    override val jdbc: NamedParameterJdbcTemplate
) : PhotoRepository, AbstractEntityRepository<Photo>() {
    internal companion object {
        const val TableName = "photo"
        const val OwnerIdColumnName = "owner_id"
        const val UploadedDateColumnName = "upload_date"
        const val ShootingDateColumnName = "shooting_date"
        const val TypeColumnName = "type"
        const val HashColumnName = "hash"
        const val NoColumnName = "no"
        const val VersionColumnName = "version"

        const val AlbumPhotoTableName = "album_photo"
        const val AlbumIdColumnName = "album_id"
        const val PhotoIdColumnName = "photo_id"
        const val OrderColumnName = "\"order\""
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

    override fun fetchAll(ownerId: UUID, offset: Int, limit: Int) = jdbc.query(
        """
        SELECT *
        FROM $tableName
        WHERE $OwnerIdColumnName = :owner_id
        ORDER BY $NoColumnName
        OFFSET :offset
        LIMIT :limit
        """,
        MapSqlParameterSource(mapOf("owner_id" to ownerId, "offset" to offset, "limit" to limit))
    ) { rs, _ -> rs.toEntity() }

    override fun fetchAllOfAlbum(albumId: UUID, userId: UUID, offset: Int, limit: Int) = jdbc.query(
        """
        SELECT $tableName.*
        FROM $AlbumPhotoTableName
        JOIN $tableName
        ON $IdColumnName = $PhotoIdColumnName
        WHERE $AlbumIdColumnName = :album_id 
            AND (
                SELECT ${AbstractAuthorizationRepository.CanReadColumnName}
                FROM ${UserPhotoAuthorizationRepositoryImpl.TableName}
                WHERE ${UserPhotoAuthorizationRepositoryImpl.PhotoIdColumnName} = $tableName.$IdColumnName
                    AND ${UserPhotoAuthorizationRepositoryImpl.UserIdColumnName} = :user_id
            ) IS NOT FALSE
        ORDER BY $OrderColumnName
        OFFSET :offset
        LIMIT :limit
        """,
        MapSqlParameterSource(mapOf("album_id" to albumId, "user_id" to userId, "offset" to offset, "limit" to limit))
    ) { rs, _ -> rs.toEntity() }

    override fun fetchByHash(ownerId: UUID, hash: String) = fetchBy(
        Column(OwnerIdColumnName, ownerId),
        Column(HashColumnName, hash)
    )

    override fun fetchNextOf(no: Int) = try {
        jdbc.queryForObject(
            """
            SELECT *
            FROM $tableName
            WHERE $NoColumnName > :no 
                AND $OwnerIdColumnName = (SELECT $OwnerIdColumnName FROM $tableName WHERE $NoColumnName = :no)
            ORDER BY $NoColumnName
            LIMIT 1
            """,
            MapSqlParameterSource(mapOf("no" to no))
        ) { rs, _ -> rs.toEntity() }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun fetchNextOf(no: Int, userId: UUID) = try {
        jdbc.queryForObject(
            """
            SELECT *
            FROM $tableName p
            WHERE p.$NoColumnName > :no AND user_can_read(p.id, :user_id)
            ORDER BY p.$NoColumnName
            LIMIT 1
            """,
            MapSqlParameterSource(mapOf("no" to no, "user_id" to userId))
        ) { rs, _ -> rs.toEntity() }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun fetchNextOf(order: Int, userId: UUID, albumId: UUID) = try {
        jdbc.queryForObject(
            """
            SELECT *
            FROM $tableName p
            JOIN $AlbumPhotoTableName ap
            ON ap.$PhotoIdColumnName = p.$IdColumnName
            JOIN ${AlbumRepositoryImpl.TableName} a
            ON a.$IdColumnName = ap.$AlbumIdColumnName
            WHERE ap.$OrderColumnName > :order
                AND (
                    SELECT upa.${AbstractAuthorizationRepository.CanReadColumnName}
                    FROM ${UserPhotoAuthorizationRepositoryImpl.TableName} upa
                    WHERE upa.${UserPhotoAuthorizationRepositoryImpl.PhotoIdColumnName} = p.$IdColumnName
                        AND upa.${UserPhotoAuthorizationRepositoryImpl.UserIdColumnName} = :user_id
                ) IS NOT FALSE
            ORDER BY ap.$OrderColumnName
            LIMIT 1
            """,
            MapSqlParameterSource(mapOf("order" to order, "user_id" to userId))
        ) { rs, _ -> rs.toEntity() }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun fetchPreviousOf(no: Int) = try {
        jdbc.queryForObject(
            """
            SELECT *
            FROM $tableName
            WHERE $NoColumnName < :no 
                AND $OwnerIdColumnName = (SELECT $OwnerIdColumnName FROM $tableName WHERE $NoColumnName = :no)
            ORDER BY $NoColumnName DESC
            LIMIT 1
            """,
            MapSqlParameterSource(mapOf("no" to no))
        ) { rs, _ -> rs.toEntity() }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun fetchPreviousOf(no: Int, userId: UUID) = try {
        jdbc.queryForObject(
            """
            SELECT *
            FROM $tableName p
            WHERE p.$NoColumnName < :no AND user_can_read(p.id, :user_id)
            ORDER BY p.$NoColumnName DESC
            LIMIT 1
            """,
            MapSqlParameterSource(mapOf("no" to no, "user_id" to userId))
        ) { rs, _ -> rs.toEntity() }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun fetchPreviousOf(order: Int, userId: UUID, albumId: UUID) = try {
        jdbc.queryForObject(
            """
            SELECT *
            FROM $tableName p
            JOIN $AlbumPhotoTableName ap
            ON ap.$PhotoIdColumnName = p.$IdColumnName
            JOIN ${AlbumRepositoryImpl.TableName} a
            ON a.$IdColumnName = ap.$AlbumIdColumnName
            WHERE ap.$OrderColumnName < :order
                AND (
                    SELECT upa.${AbstractAuthorizationRepository.CanReadColumnName}
                    FROM ${UserPhotoAuthorizationRepositoryImpl.TableName} upa
                    WHERE upa.${UserPhotoAuthorizationRepositoryImpl.PhotoIdColumnName} = p.$IdColumnName
                        AND upa.${UserPhotoAuthorizationRepositoryImpl.UserIdColumnName} = :user_id
                ) IS NOT FALSE
            ORDER BY ap.$OrderColumnName DESC
            LIMIT 1
            """,
            MapSqlParameterSource(mapOf("order" to order, "user_id" to userId))
        ) { rs, _ -> rs.toEntity() }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun ResultSet.toEntity() = Photo(
        id = getObject(IdColumnName, UUID::class.java),
        ownerId = getObject(OwnerIdColumnName, UUID::class.java),
        uploadDate = getObject(UploadedDateColumnName, OffsetDateTime::class.java),
        shootingDate = getObject(ShootingDateColumnName, LocalDate::class.java),
        type = getPhotoType(),
        hash = getString(HashColumnName),
        no = getInt(NoColumnName),
        version = getInt(VersionColumnName)
    )

    private fun ResultSet.getPhotoType() = getString(TypeColumnName).let { type ->
        PhotoTypeData.values()
            .find { it.sqlValue == type }
            ?.type
            ?: throw UnknownPhotoTypeException("Unknown photo type '$type'")
    }
}
