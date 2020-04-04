package io.ivana.dto

import java.net.URI
import java.util.*

internal class PageDtoTest : JsonTest(
    filename = "page.json",
    expectedValue = PageDto(
        content = listOf(
            PhotoDto.Simple(
                id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
                rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
                compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed")
            ),
            PhotoDto.Simple(
                id = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
                rawUri = URI("/v1/photo/b98c9b79-3cfb-49b7-873f-3b6a971c6bc8/raw"),
                compressedUri = URI("/v1/photo/b98c9b79-3cfb-49b7-873f-3b6a971c6bc8/compressed")
            )
        ),
        no = 1,
        totalItems = 100,
        totalPages = 50
    ),
    deserializeAs = typeOf<PageDto<PhotoDto.Simple>>()
)
