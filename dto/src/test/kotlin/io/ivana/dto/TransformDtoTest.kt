@file:Suppress("ClassName")

package io.ivana.dto

import org.junit.jupiter.api.Nested

internal class TransformDtoTest {
    @Nested
    inner class Rotation {
        @Nested
        inner class clockwise : JsonTest(
            filename = "transform/rotation.json",
            expectedValue = TransformDto.Rotation(90.0),
            deserializeAs = typeOf<TransformDto>()
        )
    }
}
