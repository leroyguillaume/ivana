package io.ivana.api.impl

import io.ivana.core.Photo
import io.ivana.core.PhotoRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.*

@Repository
class PhotoRepositoryImpl(
    override val jdbc: NamedParameterJdbcTemplate
) : PhotoRepository, AbstractOwnableEntityRepository<Photo>() {
    internal companion object {
        const val TableName = "photo"
        const val UploadedDateColumnName = "upload_date"
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

    override fun countOfAlbum(albumId: UUID) = jdbc.queryForObject(
        """
        SELECT COUNT($PhotoIdColumnName)
        FROM $AlbumPhotoTableName
        WHERE $AlbumIdColumnName = :album_id
        """,
        MapSqlParameterSource(mapOf("album_id" to albumId))
    ) { rs, _ -> rs.getInt(1) }

    // TODO: Remove this override and user order as parameter
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

    override fun fetchAllOfAlbum(albumId: UUID, offset: Int, limit: Int) = jdbc.query(
        """
        SELECT $tableName.*
        FROM $AlbumPhotoTableName
        JOIN $tableName
        ON $IdColumnName = $PhotoIdColumnName
        WHERE $AlbumIdColumnName = :album_id
        ORDER BY $OrderColumnName
        OFFSET :offset
        LIMIT :limit
        """,
        MapSqlParameterSource(mapOf("album_id" to albumId, "offset" to offset, "limit" to limit))
    ) { rs, _ -> rs.toEntity() }

    override fun fetchByHash(ownerId: UUID, hash: String) = fetchBy(
        Column(OwnerIdColumnName, ownerId),
        Column(HashColumnName, hash)
    )

    override fun fetchNextOf(photo: Photo) = try {
        jdbc.queryForObject(
            """
            SELECT *
            FROM $tableName
            WHERE $NoColumnName > :no 
                AND $OwnerIdColumnName = :owner_id
            ORDER BY $NoColumnName
            LIMIT 1
            """,
            MapSqlParameterSource(
                mapOf(
                    "no" to photo.no,
                    "owner_id" to photo.ownerId
                )
            )
        ) { rs, _ -> rs.toEntity() }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun fetchPreviousOf(photo: Photo) = try {
        jdbc.queryForObject(
            """
            SELECT *
            FROM $tableName
            WHERE $NoColumnName < :no 
                AND $OwnerIdColumnName = :owner_id
            ORDER BY $NoColumnName DESC
            LIMIT 1
            """,
            MapSqlParameterSource(
                mapOf(
                    "no" to photo.no,
                    "owner_id" to photo.ownerId
                )
            )
        ) { rs, _ -> rs.toEntity() }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun ResultSet.toEntity() = Photo(
        id = getObject(IdColumnName, UUID::class.java),
        ownerId = getObject(OwnerIdColumnName, UUID::class.java),
        uploadDate = getObject(UploadedDateColumnName, OffsetDateTime::class.java),
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
