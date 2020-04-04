@file:Suppress("ClassName")

package io.ivana.dto

import org.junit.jupiter.api.Nested
import java.net.URI
import java.util.*

internal class PhotoDtoTest {
    @Nested
    inner class Simple : JsonTest(
        filename = "photo/simple.json",
        expectedValue = PhotoDto.Simple(
            id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
            rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
            compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed")
        ),
        deserializeAs = typeOf<PhotoDto.Simple>()
    )

    @Nested
    inner class Navigable {
        @Nested
        inner class default : JsonTest(
            filename = "photo/navigable_default.json",
            expectedValue = PhotoDto.Navigable(
                id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
                rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
                compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed")
            ),
            deserializeAs = typeOf<PhotoDto.Navigable>()
        )

        @Nested
        inner class complete : JsonTest(
            filename = "photo/navigable_complete.json",
            expectedValue = PhotoDto.Navigable(
                id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
                rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
                compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed"),
                previous = PhotoDto.Simple(
                    id = UUID.fromString("2424105b-9c3b-40df-a535-29d62e5b409b"),
                    rawUri = URI("/v1/photo/2424105b-9c3b-40df-a535-29d62e5b409b/raw"),
                    compressedUri = URI("/v1/photo/2424105b-9c3b-40df-a535-29d62e5b409b/compressed")
                ),
                next = PhotoDto.Simple(
                    id = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
                    rawUri = URI("/v1/photo/b98c9b79-3cfb-49b7-873f-3b6a971c6bc8/raw"),
                    compressedUri = URI("/v1/photo/b98c9b79-3cfb-49b7-873f-3b6a971c6bc8/compressed")
                )
            ),
            deserializeAs = typeOf<PhotoDto.Navigable>()
        )
    }
}
