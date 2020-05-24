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

    override fun saveDeletionEvent(albumId: UUID, source: EventSource.User) =
        insert<AlbumEvent.Deletion>(
            subjectId = albumId,
            type = AlbumEventType.Deletion,
            data = AlbumEventData.Deletion(source.toData() as EventSourceData.User)
        )

    override fun saveUpdateEvent(albumId: UUID, content: AlbumEvent.Update.Content, source: EventSource.User) =
        insert<AlbumEvent.Update>(
            subjectId = albumId,
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

    override fun saveUpdatePermissionsEvent(
        albumId: UUID,
        content: AlbumEvent.UpdatePermissions.Content,
        source: EventSource.User
    ) = insert<AlbumEvent.UpdatePermissions>(
        subjectId = albumId,
        type = AlbumEventType.UpdatePermissions,
        data = AlbumEventData.UpdatePermissions(
            source = source.toData() as EventSourceData.User,
            content = AlbumEventData.UpdatePermissions.Content(
                permissionsToAdd = content.permissionsToAdd.map { it.toData() }.toSet(),
                permissionsToRemove = content.permissionsToRemove.map { it.toData() }.toSet()
            )
        )
    )

    override fun RawEvent<AlbumEventType>.toEvent() = when (type) {
        AlbumEventType.Creation -> toCreationEvent()
        AlbumEventType.Deletion -> toDeletionEvent()
        AlbumEventType.Update -> toUpdateEvent()
        AlbumEventType.UpdatePermissions -> toUpdatePermissionsEvent()
    }

    private fun RawEvent<AlbumEventType>.toCreationEvent() =
        mapper.readValue<AlbumEventData.Creation>(jsonData).let { data ->
            AlbumEvent.Creation(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource(),
                albumName = data.content.name
            )
        }

    private fun RawEvent<AlbumEventType>.toDeletionEvent() =
        mapper.readValue<AlbumEventData.Deletion>(jsonData).let { data ->
            AlbumEvent.Deletion(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource()
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

    private fun RawEvent<AlbumEventType>.toUpdatePermissionsEvent() =
        mapper.readValue<AlbumEventData.UpdatePermissions>(jsonData).let { data ->
            AlbumEvent.UpdatePermissions(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource(),
                content = AlbumEvent.UpdatePermissions.Content(
                    permissionsToAdd = data.content.permissionsToAdd.map { it.toSubjectPermissions() }.toSet(),
                    permissionsToRemove = data.content.permissionsToRemove.map { it.toSubjectPermissions() }.toSet()
                )
            )
        }
}
