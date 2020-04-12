@file:Suppress("ClassName")

package io.ivana.api.web

import io.ivana.core.Transform
import io.ivana.dto.TransformDto
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ExtensionsTest {
    @Nested
    inner class transformDtoToTransform {
        @Nested
        inner class rotation {
            @Test
            fun clockwise() {
                TransformDto.Rotation(TransformDto.Rotation.Direction.Clockwise).toTransform().shouldBe(
                    Transform.Rotation(Transform.Rotation.Direction.Clockwise)
                )
            }

            @Test
            fun counterclockwise() {
                TransformDto.Rotation(TransformDto.Rotation.Direction.Counterclockwise).toTransform().shouldBe(
                    Transform.Rotation(Transform.Rotation.Direction.Counterclockwise)
                )
            }
        }
    }
}
