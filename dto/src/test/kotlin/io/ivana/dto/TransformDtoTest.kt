@file:Suppress("ClassName")

package io.ivana.dto

import org.junit.jupiter.api.Nested

internal class TransformDtoTest {
    @Nested
    inner class Rotation {
        @Nested
        inner class clockwise : JsonTest(
            filename = "transform/rotation/clockwise.json",
            expectedValue = TransformDto.Rotation(TransformDto.Rotation.Direction.Clockwise),
            deserializeAs = typeOf<TransformDto>()
        )

        @Nested
        inner class counterclockwise : JsonTest(
            filename = "transform/rotation/counterclockwise.json",
            expectedValue = TransformDto.Rotation(TransformDto.Rotation.Direction.Counterclockwise),
            deserializeAs = typeOf<TransformDto>()
        )
    }
}
