@file:Suppress("ClassName")

package io.ivana.dto

import org.junit.jupiter.api.Nested
import java.net.URI
import java.time.LocalDate
import java.util.*

internal class PhotoDtoTest {
    @Nested
    inner class Light : JsonTest(
        filename = "photo/light.json",
        expectedValue = PhotoDto.Light(
            id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
            ownerId = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
            rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
            compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed")
        ),
        deserializeAs = typeOf<PhotoDto.Light>()
    )

    @Nested
    inner class Navigable {
        @Nested
        inner class Default : JsonTest(
            filename = "photo/navigable_default.json",
            expectedValue = PhotoDto.Complete.Navigable(
                id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
                ownerId = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
                rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
                compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed"),
                permissions = setOf(PermissionDto.Read)
            ),
            deserializeAs = typeOf<PhotoDto.Complete.Navigable>()
        )

        @Nested
        inner class Complete : JsonTest(
            filename = "photo/navigable_complete.json",
            expectedValue = PhotoDto.Complete.Navigable(
                id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
                ownerId = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
                rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
                compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed"),
                shootingDate = LocalDate.parse("2020-06-07"),
                permissions = setOf(PermissionDto.Read),
                previous = PhotoDto.Light(
                    id = UUID.fromString("2424105b-9c3b-40df-a535-29d62e5b409b"),
                    ownerId = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
                    rawUri = URI("/v1/photo/2424105b-9c3b-40df-a535-29d62e5b409b/raw"),
                    compressedUri = URI("/v1/photo/2424105b-9c3b-40df-a535-29d62e5b409b/compressed")
                ),
                next = PhotoDto.Light(
                    id = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
                    ownerId = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
                    rawUri = URI("/v1/photo/b98c9b79-3cfb-49b7-873f-3b6a971c6bc8/raw"),
                    compressedUri = URI("/v1/photo/b98c9b79-3cfb-49b7-873f-3b6a971c6bc8/compressed")
                )
            ),
            deserializeAs = typeOf<PhotoDto.Complete.Navigable>()
        )
    }

    @Nested
    inner class Simple {
        @Nested
        inner class Default : JsonTest(
            filename = "photo/simple_default.json",
            expectedValue = PhotoDto.Complete.Simple(
                id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
                ownerId = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
                rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
                compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed"),
                permissions = setOf(PermissionDto.Read)
            ),
            deserializeAs = typeOf<PhotoDto.Complete.Simple>()
        )

        @Nested
        inner class Complete : JsonTest(
            filename = "photo/simple_complete.json",
            expectedValue = PhotoDto.Complete.Simple(
                id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
                ownerId = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
                rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
                compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed"),
                shootingDate = LocalDate.parse("2020-06-07"),
                permissions = setOf(PermissionDto.Read)
            ),
            deserializeAs = typeOf<PhotoDto.Complete.Simple>()
        )
    }
}
