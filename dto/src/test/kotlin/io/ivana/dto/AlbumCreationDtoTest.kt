@file:Suppress("ClassName")

package io.ivana.dto

internal class AlbumCreationDtoTest : JsonTest(
    filename = "album-creation.json",
    expectedValue = AlbumCreationDto("album"),
    deserializeAs = typeOf<AlbumCreationDto>()
)
