package io.ivana.api.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ivana.core.AlbumEvent
import io.ivana.core.AlbumEventRepository
import io.ivana.core.EventSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class AlbumEventRepositoryImpl(
    jdbc: NamedParameterJdbcTemplate,
    mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
) : AlbumEventRepository, AbstractEventRepository<AlbumEvent, AlbumEventType>(jdbc, mapper) {
    internal companion object {
        const val TableName = "album_event"
    }

    override val tableName = TableName
    override val eventTypes = AlbumEventType.values()

    override fun saveCreationEvent(name: String, source: EventSource.User) =
        insert<AlbumEvent.Creation>(
            subjectId = UUID.randomUUID(),
            type = AlbumEventType.Creation,
            data = AlbumEventData.Creation(
                source = source.toData() as EventSourceData.User,
                content = AlbumEventData.Creation.Content(name)
            )
        )

    override fun RawEvent<AlbumEventType>.toEvent() = when (type) {
        AlbumEventType.Creation -> toCreationEvent()
    }

    private fun RawEvent<AlbumEventType>.toCreationEvent() =
        mapper.readValue<AlbumEventData.Creation>(jsonData).let { data ->
            AlbumEvent.Creation(
                date = date,
                subjectId = subjectId,
                source = data.source.toSource(),
                albumName = data.content.name
            )
        }
}
