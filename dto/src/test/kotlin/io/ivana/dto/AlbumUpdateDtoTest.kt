@file:Suppress("ClassName")

package io.ivana.dto

import java.util.*

internal class AlbumUpdateDtoTest : JsonTest(
    filename = "album-update.json",
    expectedValue = AlbumUpdateDto(
        name = "album",
        photosToAdd = listOf(UUID.fromString("c33d6e4a-6c55-4ed8-a23a-0c36dae1cc91")),
        photosToRemove = listOf(UUID.fromString("1890c0d1-c5db-4563-b43c-7401807e0353"))
    ),
    deserializeAs = typeOf<AlbumUpdateDto>()
)
