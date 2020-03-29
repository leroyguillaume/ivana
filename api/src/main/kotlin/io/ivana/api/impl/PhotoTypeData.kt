package io.ivana.api.impl

import com.fasterxml.jackson.annotation.JsonProperty
import io.ivana.core.Photo

private const val JpgSqlValue = "jpg"
private const val PngSqlValue = "png"

internal enum class PhotoTypeData(
    val type: Photo.Type,
    val sqlValue: String
) {
    @JsonProperty(JpgSqlValue)
    Jpg(Photo.Type.Jpg, JpgSqlValue),

    @JsonProperty(PngSqlValue)
    Png(Photo.Type.Png, PngSqlValue)
}
