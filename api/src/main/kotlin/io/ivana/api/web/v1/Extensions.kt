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

fun Photo.toDto() = PhotoDto(
    id = id,
    rawUri = URI("$PhotoApiEndpoint/$id$RawPhotoEndpoint"),
    compressedUri = URI("$PhotoApiEndpoint/$id$CompressedPhotoEndpoint"),
    no = no
)
