@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.Photo
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PhotoTypeDataTest {
    @Nested
    inner class jpg : JsonTest(
        filename = "event-data/photo-type/jpg.json",
        expectedValue = Wrapper(PhotoTypeData.Jpg),
        deserializeAs = typeOf<Wrapper>()
    ) {
        private val typeData = PhotoTypeData.Jpg
        private val type = Photo.Type.Jpg
        private val sqlValue = "jpg"

        @Test
        fun type() {
            typeData.type shouldBe type
        }

        @Test
        fun sqlValue() {
            typeData.sqlValue shouldBe sqlValue
        }
    }

    @Nested
    inner class png : JsonTest(
        filename = "event-data/photo-type/png.json",
        expectedValue = Wrapper(PhotoTypeData.Png),
        deserializeAs = typeOf<Wrapper>()
    ) {
        private val typeData = PhotoTypeData.Png
        private val type = Photo.Type.Png
        private val sqlValue = "png"

        @Test
        fun type() {
            typeData.type shouldBe type
        }

        @Test
        fun sqlValue() {
            typeData.sqlValue shouldBe sqlValue
        }
    }

    private data class Wrapper(
        val type: PhotoTypeData
    )
}
