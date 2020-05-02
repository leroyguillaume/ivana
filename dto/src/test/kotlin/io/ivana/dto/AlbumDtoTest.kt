@file:Suppress("ClassName")

package io.ivana.dto

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

internal class AlbumDtoTest : JsonTest(
    filename = "album.json",
    expectedValue = AlbumDto(
        id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
        name = "album",
        ownerId = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
        creationDate = OffsetDateTime.of(2020, 4, 13, 14, 55, 0, 0, ZoneOffset.UTC)
    ),
    deserializeAs = typeOf<AlbumDto>()
)
