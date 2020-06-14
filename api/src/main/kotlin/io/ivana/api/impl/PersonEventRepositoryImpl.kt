package io.ivana.api.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ivana.core.EventSource
import io.ivana.core.PersonEvent
import io.ivana.core.PersonEventRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PersonEventRepositoryImpl(
    jdbc: NamedParameterJdbcTemplate,
    mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
) : PersonEventRepository, AbstractEventRepository<PersonEvent, PersonEventType>(jdbc, mapper) {
    internal companion object {
        const val TableName = "person_event"
    }

    override val tableName = TableName
    override val eventTypes = PersonEventType.values()

    override fun saveCreationEvent(content: PersonEvent.Creation.Content, source: EventSource.User) =
        insert<PersonEvent.Creation>(
            subjectId = UUID.randomUUID(),
            type = PersonEventType.Creation,
            data = PersonEventData.Creation(
                source = source.toData() as EventSourceData.User,
                content = PersonEventData.Creation.Content(
                    lastName = content.lastName,
                    firstName = content.firstName
                )
            )
        )

    override fun saveDeletionEvent(personId: UUID, source: EventSource.User) =
        insert<PersonEvent.Deletion>(
            subjectId = personId,
            type = PersonEventType.Deletion,
            data = PersonEventData.Deletion(source.toData() as EventSourceData.User)
        )

    override fun saveUpdateEvent(
        personId: UUID,
        content: PersonEvent.Update.Content,
        source: EventSource.User
    ) = insert<PersonEvent.Update>(
        subjectId = personId,
        type = PersonEventType.Update,
        data = PersonEventData.Update(
            source = source.toData() as EventSourceData.User,
            content = PersonEventData.Update.Content(
                lastName = content.lastName,
                firstName = content.firstName
            )
        )
    )

    override fun RawEvent<PersonEventType>.toEvent() = when (type) {
        PersonEventType.Creation -> toCreationEvent()
        PersonEventType.Deletion -> toDeletionEvent()
        PersonEventType.Update -> toUpdateEvent()
    }

    private fun RawEvent<PersonEventType>.toCreationEvent() =
        mapper.readValue<PersonEventData.Creation>(jsonData).let { data ->
            PersonEvent.Creation(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource(),
                content = PersonEvent.Creation.Content(
                    lastName = data.content.lastName,
                    firstName = data.content.firstName
                )
            )
        }

    private fun RawEvent<PersonEventType>.toDeletionEvent() =
        mapper.readValue<PersonEventData.Deletion>(jsonData).let { data ->
            PersonEvent.Deletion(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource()
            )
        }

    private fun RawEvent<PersonEventType>.toUpdateEvent() =
        mapper.readValue<PersonEventData.Update>(jsonData).let { data ->
            PersonEvent.Update(
                date = date,
                subjectId = subjectId,
                number = number,
                source = data.source.toSource(),
                content = PersonEvent.Update.Content(
                    lastName = data.content.lastName,
                    firstName = data.content.firstName
                )
            )
        }
}
