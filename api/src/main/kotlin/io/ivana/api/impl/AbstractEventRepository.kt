package io.ivana.api.impl

import com.fasterxml.jackson.databind.ObjectMapper
import io.ivana.core.Event
import io.ivana.core.EventRepository
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.time.OffsetDateTime
import java.util.*

abstract class AbstractEventRepository<E : Event, T : EventType> constructor(
    protected val jdbc: NamedParameterJdbcTemplate,
    protected val mapper: ObjectMapper
) : EventRepository<E> {
    internal companion object {
        const val DateColumnName = "date"
        const val SubjectIdColumnName = "subject_id"
        const val NumberColumnName = "number"
        const val TypeColumnName = "type"
        const val DataColumnName = "data"
    }

    protected abstract val tableName: String
    protected abstract val eventTypes: Array<T>

    override fun fetch(number: Long): E? = jdbc.query(
        """
        SELECT *
        FROM $tableName
        WHERE $NumberColumnName = :number
        """,
        MapSqlParameterSource(mapOf("number" to number)),
        ResultSetExtractor { rs ->
            if (rs.next()) {
                val rawEvent = RawEvent(
                    type = eventTypeFromSqlType(rs.getString(TypeColumnName)),
                    date = rs.getObject(DateColumnName, OffsetDateTime::class.java),
                    subjectId = rs.getObject(SubjectIdColumnName, UUID::class.java),
                    number = rs.getLong(NumberColumnName),
                    jsonData = rs.getString(DataColumnName)
                )
                rawEvent.toEvent()
            } else {
                null
            }
        }
    )

    @Suppress("SqlResolve", "UNCHECKED_CAST")
    internal inline fun <reified EE : E> insert(subjectId: UUID, type: T, data: EventData): EE {
        val keyHolder = GeneratedKeyHolder()
        jdbc.jdbcTemplate.update(
            { connection ->
                connection.prepareStatement(
                    """
                    INSERT INTO $tableName ($SubjectIdColumnName, $TypeColumnName, $DataColumnName)
                    VALUES (?, ?::${type.sqlType}, ?::jsonb)
                    """,
                    arrayOf(NumberColumnName)
                ).apply {
                    setObject(1, subjectId)
                    setString(2, type.sqlValue)
                    setString(3, mapper.writeValueAsString(data))
                }
            },
            keyHolder
        )
        val keys = keyHolder.keys!!
        return fetch(keys[NumberColumnName] as Long) as EE
    }

    protected fun eventTypeFromSqlType(sqlEventType: String) = eventTypes
        .firstOrNull { it.sqlValue == sqlEventType }
        ?: throw UnknownEventTypeException("Unknown event type '$sqlEventType'")

    internal data class RawEvent<T : EventType>(
        val type: T,
        val date: OffsetDateTime,
        val subjectId: UUID,
        val number: Long,
        val jsonData: String
    )

    internal abstract fun RawEvent<T>.toEvent(): E
}
