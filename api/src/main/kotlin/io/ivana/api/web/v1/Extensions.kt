package io.ivana.api.web.v1

import io.ivana.core.Photo
import io.ivana.dto.PhotoDto
import java.net.URI

const val EndpointRoot = "/v1"

const val LoginEndpoint = "$EndpointRoot/login"
const val LogoutEndpoint = "$EndpointRoot/logout"
const val PhotoApiEndpoint = "$EndpointRoot/photo"
const val RawPhotoEndpoint = "/raw"
const val CompressedPhotoEndpoint = "/compressed"

const val FilesParamName = "files"

const val UuidRegex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"

fun Photo.toDto() = PhotoDto(
    id = id,
    rawUri = URI("$PhotoApiEndpoint/$id$RawPhotoEndpoint"),
    compressedUri = URI("$PhotoApiEndpoint/$id$CompressedPhotoEndpoint"),
    no = no
)
