package io.ivana.api.impl

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.ivana.core.EventSource
import java.net.InetAddress
import java.util.*

private const val SystemTypeValue = "system"
private const val UserTypeValue = "user"

@JsonTypeInfo(
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EventSourceData.System::class, name = SystemTypeValue),
    JsonSubTypes.Type(value = EventSourceData.User::class, name = UserTypeValue)
)
internal sealed class EventSourceData {
    enum class Type {
        @JsonProperty(SystemTypeValue)
        System,

        @JsonProperty(UserTypeValue)
        User
    }

    object System : EventSourceData() {
        override val type = Type.System

        override fun equals(other: Any?) = other is System

        override fun toSource() = EventSource.System
    }

    data class User(
        val id: UUID,
        val ip: InetAddress
    ) : EventSourceData() {
        override val type = Type.User

        override fun toSource() = EventSource.User(id, ip)
    }

    abstract val type: Type

    abstract fun toSource(): EventSource
}
