package io.ivana.dto

import java.net.URI
import java.util.*

internal class PhotoDtoTest : JsonTest(
    filename = "photo.json",
    expectedValue = PhotoDto(
        id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
        rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
        compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed")
    ),
    deserializeAs = typeOf<PhotoDto>()
)
