package io.ivana.api.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ivana.core.EventSource
import io.ivana.core.PhotoEvent
import io.ivana.core.PhotoEventRepository
import io.ivana.core.Transform
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PhotoEventRepositoryImpl(
    jdbc: NamedParameterJdbcTemplate,
    mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
) : PhotoEventRepository, AbstractEventRepository<PhotoEvent, PhotoEventType>(jdbc, mapper) {
    internal companion object {
        const val TableName = "photo_event"
    }

    override val tableName = TableName
    override val eventTypes = PhotoEventType.values()

    override fun saveDeletionEvent(photoId: UUID, source: EventSource.User) =
        insert<PhotoEvent.Deletion>(
            subjectId = photoId,
            type = PhotoEventType.Deletion,
            data = PhotoEventData.Deletion(source.toData() as EventSourceData.User)
        )

    override fun saveTransformEvent(photoId: UUID, transform: Transform, source: EventSource.User) =
        insert<PhotoEvent.Transform>(
            subjectId = photoId,
            type = PhotoEventType.Transform,
            data = PhotoEventData.Transform(
                source = source.toData() as EventSourceData.User,
                content = transform.toEventDataContent()
            )
        )

    override fun saveUpdateEvent(
        photoId: UUID,
        content: PhotoEvent.Update.Content,
        source: EventSource.User
    ) = insert<PhotoEvent.Update>(
        subjectId = photoId,
        type = PhotoEventType.Update,
        data = PhotoEventData.Update(
            source = source.toData() as EventSourceData.User,
            content = PhotoEventData.Update.Content(
                shootingDate = content.shootingDate
            )
        )
    )

    override fun saveUpdatePermissionsEvent(
        photoId: UUID,
        content: PhotoEvent.UpdatePermissions.Content,
        source: EventSource.User
    ) = insert<PhotoEvent.UpdatePermissions>(
        subjectId = photoId,
        type = PhotoEventType.UpdatePermissions,
        data = PhotoEventData.UpdatePermissions(
            source = source.toData() as EventSourceData.User,
            content = PhotoEventData.UpdatePermissions.Content(
                permissionsToAdd = content.permissionsToAdd.map { it.toData() }.toSet(),
                permissionsToRemove = content.permissionsToRemove.map { it.toData() }.toSet()
            )
        )
    )

    override fun saveUploadEvent(content: PhotoEvent.Upload.Content, source: EventSource.User) =
        insert<PhotoEvent.Upload>(
            subjectId = UUID.randomUUID(),
            type = PhotoEventType.Upload,
            data = PhotoEventData.Upload(
                source = source.toData() as EventSourceData.User,
                content = PhotoEventData.Upload.Content(
                    type = content.type.toPhotoTypeData(),
                    hash = content.hash
                )
            )
        )

    override fun RawEvent<PhotoEventType>.toEvent() = when (type) {
        PhotoEventType.Deletion -> toDeletionEvent()
        PhotoEventType.Transform -> toTransformEvent()
        PhotoEventType.Update -> toUpdateEvent()
        PhotoEventType.UpdatePermissions -> toUpdatePermissionsEvent()
        PhotoEventType.Upload -> toUploadEvent()
    }

    private fun PhotoEventData.Transform.toTransform() = when (content) {
        is PhotoEventData.Transform.Content.Rotation -> Transform.Rotation(content.degrees)
    }

    private fun RawEvent<PhotoEventType>.toDeletionEvent() =
        mapper.readValue<PhotoEventData.Deletion>(jsonData).let { data ->
            PhotoEvent.Deletion(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource()
            )
        }

    private fun RawEvent<PhotoEventType>.toTransformEvent() =
        mapper.readValue<PhotoEventData.Transform>(jsonData).let { data ->
            PhotoEvent.Transform(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource(),
                transform = data.toTransform()
            )
        }

    private fun RawEvent<PhotoEventType>.toUpdateEvent() =
        mapper.readValue<PhotoEventData.Update>(jsonData).let { data ->
            PhotoEvent.Update(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource(),
                content = PhotoEvent.Update.Content(
                    shootingDate = data.content.shootingDate
                )
            )
        }

    private fun RawEvent<PhotoEventType>.toUpdatePermissionsEvent() =
        mapper.readValue<PhotoEventData.UpdatePermissions>(jsonData).let { data ->
            PhotoEvent.UpdatePermissions(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource(),
                content = PhotoEvent.UpdatePermissions.Content(
                    permissionsToAdd = data.content.permissionsToAdd.map { it.toSubjectPermissions() }.toSet(),
                    permissionsToRemove = data.content.permissionsToRemove.map { it.toSubjectPermissions() }.toSet()
                )
            )
        }

    private fun RawEvent<PhotoEventType>.toUploadEvent() =
        mapper.readValue<PhotoEventData.Upload>(jsonData).let { data ->
            PhotoEvent.Upload(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource(),
                content = PhotoEvent.Upload.Content(
                    type = data.content.type.type,
                    hash = data.content.hash
                )
            )
        }

    private fun Transform.toEventDataContent() = when (this) {
        is Transform.Rotation -> PhotoEventData.Transform.Content.Rotation(degrees)
    }
}
