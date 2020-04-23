package io.ivana.api.impl

import io.ivana.core.Album
import io.ivana.core.AlbumRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.*

@Repository
class AlbumRepositoryImpl(
    override val jdbc: NamedParameterJdbcTemplate
) : AlbumRepository, AbstractOwnableEntityRepository<Album>() {
    internal companion object {
        const val TableName = "album"
        const val NameColumnName = "name"
        const val CreationDateColumnName = "creation_date"
    }

    override val tableName = TableName

    override fun entityFromResultSet(rs: ResultSet) = Album(
        id = rs.getObject(IdColumnName, UUID::class.java),
        ownerId = rs.getObject(OwnerIdColumnName, UUID::class.java),
        name = rs.getString(NameColumnName),
        creationDate = rs.getObject(CreationDateColumnName, OffsetDateTime::class.java)
    )
}
