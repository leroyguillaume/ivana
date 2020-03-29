package io.ivana.api.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ivana.core.EventSource
import io.ivana.core.UserEvent
import io.ivana.core.UserEventRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserEventRepositoryImpl(
    jdbc: NamedParameterJdbcTemplate,
    mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
) : UserEventRepository, AbstractEventRepository<UserEvent, UserEventType>(jdbc, mapper) {
    internal companion object {
        const val TableName = "user_event"
    }

    override val tableName = TableName
    override val eventTypes = UserEventType.values()

    override fun saveCreationEvent(content: UserEvent.Creation.Content, source: EventSource) =
        insert<UserEvent.Creation>(
            subjectId = UUID.randomUUID(),
            type = UserEventType.Creation,
            data = UserEventData.Creation(
                source = source.toData(),
                content = UserEventData.Creation.Content(
                    name = content.name,
                    hashedPwd = content.hashedPwd,
                    role = content.role.toRoleData()
                )
            )
        )

    override fun saveLoginEvent(source: EventSource.User) = insert<UserEvent.Login>(
        source.id, UserEventType.Login, UserEventData.Login(source.toData() as EventSourceData.User)
    )

    override fun RawEvent<UserEventType>.toEvent() = when (type) {
        UserEventType.Creation -> toCreationEvent()
        UserEventType.Login -> toLoginEvent()
    }

    private fun RawEvent<UserEventType>.toCreationEvent() =
        mapper.readValue<UserEventData.Creation>(jsonData).let { data ->
            UserEvent.Creation(
                date = date,
                subjectId = subjectId,
                source = data.source.toSource(),
                content = UserEvent.Creation.Content(
                    name = data.content.name,
                    hashedPwd = data.content.hashedPwd,
                    role = data.content.role.role
                )
            )
        }

    private fun RawEvent<UserEventType>.toLoginEvent() = mapper.readValue<UserEventData.Login>(jsonData).let { data ->
        UserEvent.Login(
            date = date,
            subjectId = subjectId,
            number = number,
            source = data.source.toSource()
        )
    }
}
