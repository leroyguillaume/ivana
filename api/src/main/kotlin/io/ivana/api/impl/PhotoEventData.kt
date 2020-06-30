package io.ivana.api.impl

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate
import java.util.*

private const val RotationTransformTypeValue = "rotation"

internal sealed class PhotoEventData : EventData {
    data class Deletion(
        override val source: EventSourceData.User
    ) : PhotoEventData()

    data class Transform(
        override val source: EventSourceData.User,
        val content: Content
    ) : PhotoEventData() {
        @JsonTypeInfo(
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            use = JsonTypeInfo.Id.NAME,
            property = "type"
        )
        @JsonSubTypes(
            JsonSubTypes.Type(value = Content.Rotation::class, name = RotationTransformTypeValue)
        )
        sealed class Content {
            enum class Type {
                @JsonProperty(RotationTransformTypeValue)
                Rotation
            }

            data class Rotation(
                val degrees: Double
            ) : Content() {
                override val type = Type.Rotation
            }

            abstract val type: Type
        }
    }

    data class Update(
        override val source: EventSourceData.User,
        val content: Content
    ) : PhotoEventData() {
        data class Content(
            val shootingDate: LocalDate? = null
        )
    }

    data class UpdatePeople(
        override val source: EventSourceData.User,
        val content: Content
    ) : PhotoEventData() {
        data class Content(
            val peopleToAdd: Set<UUID>,
            val peopleToRemove: Set<UUID>
        )
    }

    data class UpdatePermissions(
        override val source: EventSourceData.User,
        val content: Content
    ) : PhotoEventData() {
        data class Content(
            val permissionsToAdd: Set<SubjectPermissionsData>,
            val permissionsToRemove: Set<SubjectPermissionsData>
        )
    }

    data class Upload(
        override val source: EventSourceData.User,
        val content: Content
    ) : PhotoEventData() {
        data class Content(
            val type: PhotoTypeData,
            val hash: String
        )
    }
}
