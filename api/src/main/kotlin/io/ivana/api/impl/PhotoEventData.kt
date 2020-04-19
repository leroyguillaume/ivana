package io.ivana.api.impl

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

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
                val direction: Direction
            ) : Content() {
                enum class Direction(
                    val direction: io.ivana.core.Transform.Rotation.Direction
                ) {
                    @JsonProperty("clockwise")
                    Clockwise(io.ivana.core.Transform.Rotation.Direction.Clockwise),

                    @JsonProperty("counterclockwise")
                    Counterclockwise(io.ivana.core.Transform.Rotation.Direction.Counterclockwise)
                }

                override val type = Type.Rotation
            }

            abstract val type: Type
        }
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
