package io.ivana.api.impl

import io.ivana.core.Person
import io.ivana.core.PersonRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*

@Repository
class PersonRepositoryImpl(
    override val jdbc: NamedParameterJdbcTemplate
) : PersonRepository, AbstractEntityRepository<Person>() {
    internal companion object {
        const val TableName = "person"
        const val LastNameColumnName = "last_name"
        const val FirstNameColumnName = "first_name"

        const val PhotoPersonTableName = "photo_person"
        const val PhotoIdColumnName = "photo_id"
        const val PersonIdColumnName = "person_id"
    }

    override val tableName = TableName

    override fun fetchByName(lastName: String, firstName: String) = try {
        jdbc.queryForObject(
            """
            SELECT *
            FROM $TableName
            WHERE $LastNameColumnName = :last_name AND $FirstNameColumnName = :first_name
            """,
            MapSqlParameterSource(mapOf("last_name" to lastName, "first_name" to firstName))
        ) { rs, _ -> rs.toEntity() }
    } catch (exception: EmptyResultDataAccessException) {
        null
    }

    override fun fetchOn(photoId: UUID) = jdbc.query(
        """
        SELECT p.*
        FROM $PhotoPersonTableName pp
        JOIN $TableName p
        ON p.$IdColumnName = pp.$PersonIdColumnName
        WHERE pp.$PhotoIdColumnName = :photo_id
        ORDER BY p.$LastNameColumnName, p.$FirstNameColumnName
        """,
        MapSqlParameterSource(mapOf("photo_id" to photoId))
    ) { rs, _ -> rs.toEntity() }

    override fun suggest(name: String, count: Int) = jdbc.query(
        """
        SELECT *
        FROM $TableName
        WHERE (LOWER($FirstNameColumnName) || ' ' || LOWER($LastNameColumnName)) LIKE LOWER('%' || :name || '%') 
        ORDER BY $FirstNameColumnName || ' ' || $LastNameColumnName
        LIMIT :limit
        """,
        MapSqlParameterSource(mapOf("name" to name, "limit" to count))
    ) { rs, _ -> rs.toEntity() }

    override fun ResultSet.toEntity() = Person(
        id = getObject(IdColumnName, UUID::class.java),
        lastName = getString(LastNameColumnName),
        firstName = getString(FirstNameColumnName)
    )
}
