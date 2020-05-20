package io.ivana.api.impl

import io.ivana.core.Album
import io.ivana.core.AlbumRepository
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

    override fun fetchAll(ownerId: UUID, offset: Int, limit: Int) = jdbc.query(
        """
        SELECT *
        FROM $tableName
        WHERE $OwnerIdColumnName = :owner_id
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

    override fun ResultSet.toEntity() = Album(
        id = getObject(IdColumnName, UUID::class.java),
        ownerId = getObject(OwnerIdColumnName, UUID::class.java),
        name = getString(NameColumnName),
        creationDate = getObject(CreationDateColumnName, OffsetDateTime::class.java)
    )
}
