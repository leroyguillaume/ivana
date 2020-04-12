package io.ivana.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

private const val RotationTypeValue = "rotation"

@JsonTypeInfo(
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TransformDto.Rotation::class, name = RotationTypeValue)
)
sealed class TransformDto {
    enum class Type {
        @JsonProperty(RotationTypeValue)
        Rotation
    }

    data class Rotation(
        val direction: Direction
    ) : TransformDto() {
        enum class Direction {
            @JsonProperty("clockwise")
            Clockwise,

            @JsonProperty("counterclockwise")
            Counterclockwise
        }

        override val type = Type.Rotation
    }

    abstract val type: Type
}
