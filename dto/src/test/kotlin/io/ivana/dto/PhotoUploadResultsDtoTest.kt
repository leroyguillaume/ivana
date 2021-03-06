package io.ivana.dto

import java.net.URI
import java.util.*

internal class PhotoUploadResultsDtoTest : JsonTest(
    filename = "photo-upload-results.json",
    expectedValue = PhotoUploadResultsDto(
        listOf(
            PhotoUploadResultsDto.Result.Success(
                PhotoDto.Light(
                    id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
                    ownerId = UUID.fromString("b98c9b79-3cfb-49b7-873f-3b6a971c6bc8"),
                    rawUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/raw"),
                    compressedUri = URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361/compressed")
                )
            ),
            PhotoUploadResultsDto.Result.Failure(ErrorDto.InternalError)
        )
    ),
    deserializeAs = typeOf<PhotoUploadResultsDto>()
)
