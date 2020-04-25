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

    override fun saveUpdateEvent(id: UUID, content: AlbumEvent.Update.Content, source: EventSource.User) =
        insert<AlbumEvent.Update>(
            subjectId = id,
            type = AlbumEventType.Update,
            data = AlbumEventData.Update(
                source = source.toData() as EventSourceData.User,
                content = AlbumEventData.Update.Content(
                    name = content.name,
                    photosToAdd = content.photosToAdd,
                    photosToRemove = content.photosToRemove
                )
            )
        )

    override fun RawEvent<AlbumEventType>.toEvent() = when (type) {
        AlbumEventType.Creation -> toCreationEvent()
        AlbumEventType.Update -> toUpdateEvent()
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

    private fun RawEvent<AlbumEventType>.toUpdateEvent() =
        mapper.readValue<AlbumEventData.Update>(jsonData).let { data ->
            AlbumEvent.Update(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource(),
                content = AlbumEvent.Update.Content(
                    name = data.content.name,
                    photosToAdd = data.content.photosToAdd,
                    photosToRemove = data.content.photosToRemove
                )
            )
        }
}
