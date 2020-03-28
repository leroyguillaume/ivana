package io.ivana.api.impl

import com.fasterxml.jackson.databind.ObjectMapper
import io.ivana.core.Event
import io.ivana.core.EventRepository
import org.postgresql.util.PGobject
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

abstract class AbstractEventRepository<E : Event, T : EventType> constructor(
    protected val jdbc: JdbcTemplate,
    protected val mapper: ObjectMapper
) : EventRepository<E> {
    protected companion object {
        const val DateColumnName = "date"
        const val SubjectIdColumnName = "subject_id"
        const val NumberColumnName = "number"
        const val TypeColumnName = "type"
        const val DataColumnName = "data"
    }

    protected abstract val tableName: String
    protected abstract val eventTypes: Array<T>

    override fun fetch(subjectId: UUID, number: Long): E? = jdbc.query(
        """
        SELECT *
        FROM $tableName
        WHERE $SubjectIdColumnName = ? AND $NumberColumnName = ?
        """,
        ResultSetExtractor { rs ->
            if (rs.next()) {
                val rawEvent = RawEvent(
                    type = eventTypeFromSqlType(rs.getString(TypeColumnName)),
                    date = rs.getObject(DateColumnName, OffsetDateTime::class.java).toInstant(),
                    subjectId = subjectId,
                    number = rs.getLong(NumberColumnName),
                    jsonData = rs.getString(DataColumnName)
                )
                rawEvent.toEvent()
            } else {
                null
            }
        },
        subjectId, number
    )

    @Suppress("SqlResolve", "UNCHECKED_CAST")
    internal inline fun <reified EE : E> insert(subjectId: UUID, type: T, data: EventData): EE {
        val keyHolder = GeneratedKeyHolder()
        jdbc.update(
            { connection ->
                connection.prepareStatement(
                    """
                    INSERT INTO $tableName ($SubjectIdColumnName, $NumberColumnName, $TypeColumnName, $DataColumnName)
                    VALUES (?, next_${tableName}_number(?), ?::${type.sqlType}, ?::jsonb)
                    """,
                    arrayOf(DateColumnName, NumberColumnName, DataColumnName)
                ).apply {
                    setObject(1, subjectId)
                    setObject(2, subjectId)
                    setString(3, type.sqlValue)
                    setString(4, mapper.writeValueAsString(data))
                }
            },
            keyHolder
        )
        val keys = keyHolder.keys!!
        val rawEvent = RawEvent(
            type = type,
            date = (keys[DateColumnName] as Date).toInstant(),
            subjectId = subjectId,
            number = keys[NumberColumnName] as Long,
            jsonData = (keys[DataColumnName] as PGobject).value
        )
        return rawEvent.toEvent() as EE
    }

    protected fun eventTypeFromSqlType(sqlEventType: String) = eventTypes
        .firstOrNull { it.sqlValue == sqlEventType }
        ?: throw UnknownEventTypeException("Unknown event type '$sqlEventType'")

    internal data class RawEvent<T : EventType>(
        val type: T,
        val date: Instant,
        val subjectId: UUID,
        val number: Long,
        val jsonData: String
    )

    internal abstract fun RawEvent<T>.toEvent(): E
}
