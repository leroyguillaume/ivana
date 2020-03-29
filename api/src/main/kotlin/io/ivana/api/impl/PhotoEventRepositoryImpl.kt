package io.ivana.api.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ivana.core.EventSource
import io.ivana.core.PhotoEvent
import io.ivana.core.PhotoEventRepository
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
        PhotoEventType.Upload -> toUploadEvent()
    }

    private fun RawEvent<PhotoEventType>.toUploadEvent() =
        mapper.readValue<PhotoEventData.Upload>(jsonData).let { data ->
            PhotoEvent.Upload(
                date = date,
                subjectId = subjectId,
                source = data.source.toSource(),
                content = PhotoEvent.Upload.Content(
                    type = data.content.type.type,
                    hash = data.content.hash
                )
            )
        }
}
