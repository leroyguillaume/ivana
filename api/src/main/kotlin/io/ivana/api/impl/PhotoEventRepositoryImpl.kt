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
        PhotoEventType.Upload -> toUploadEvent()
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

    private fun PhotoEventData.Transform.toTransform() = when (content) {
        is PhotoEventData.Transform.Content.Rotation -> Transform.Rotation(content.degrees)
    }

    private fun Transform.toEventDataContent() = when (this) {
        is Transform.Rotation -> PhotoEventData.Transform.Content.Rotation(degrees)
    }
}
