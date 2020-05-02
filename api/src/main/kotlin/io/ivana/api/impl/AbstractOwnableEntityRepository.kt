package io.ivana.api.impl

import io.ivana.core.OwnableEntity
import io.ivana.core.OwnableEntityRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.util.*

abstract class AbstractOwnableEntityRepository<E : OwnableEntity>
    : OwnableEntityRepository<E>, AbstractEntityRepository<E>() {
    internal companion object {
        const val OwnerIdColumnName = "owner_id"
    }

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
}
