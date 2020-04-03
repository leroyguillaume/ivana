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
    jdbc: NamedParameterJdbcTemplate
) : PhotoRepository, AbstractEntityRepository<Photo>(jdbc) {
    internal companion object {
        const val TableName = "photo"
        const val OwnerIdColumnName = "owner_id"
        const val UploadedDateColumnName = "upload_date"
        const val TypeColumnName = "type"
        const val HashColumnName = "hash"
        const val NoColumnName = "no"
    }

    override val tableName = TableName

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
        ) { rs, _ -> entityFromResultSet(rs) }
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
        ) { rs, _ -> entityFromResultSet(rs) }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun entityFromResultSet(rs: ResultSet) = Photo(
        id = rs.getObject(IdColumnName, UUID::class.java),
        ownerId = rs.getObject(OwnerIdColumnName, UUID::class.java),
        uploadDate = rs.getObject(UploadedDateColumnName, OffsetDateTime::class.java),
        type = rs.getPhotoType(),
        hash = rs.getString(HashColumnName),
        no = rs.getInt(NoColumnName)
    )

    private fun ResultSet.getPhotoType() = getString(TypeColumnName).let { type ->
        PhotoTypeData.values()
            .find { it.sqlValue == type }
            ?.type
            ?: throw UnknownPhotoTypeException("Unknown photo type '$type'")
    }
}
