package io.ivana.api.web.v1

import io.ivana.core.Photo
import io.ivana.core.PhotosTimeWindow
import io.ivana.dto.NavigablePhotoDto
import io.ivana.dto.PhotoDto
import java.net.URI
import java.util.*

const val EndpointRoot = "/v1"

const val LoginEndpoint = "$EndpointRoot/login"
const val LogoutEndpoint = "$EndpointRoot/logout"
const val PhotoApiEndpoint = "$EndpointRoot/photo"
const val RawPhotoEndpoint = "/raw"
const val CompressedPhotoEndpoint = "/compressed"

const val FilesParamName = "files"
const val NavigableParamName = "navigable"

const val UuidRegex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"

fun Photo.toDto() = PhotoDto(
    id = id,
    rawUri = rawUri(id),
    compressedUri = compressedUri(id)
)

fun PhotosTimeWindow.toDto() = NavigablePhotoDto(
    id = current.id,
    rawUri = rawUri(current.id),
    compressedUri = compressedUri(current.id),
    previous = previous?.toDto(),
    next = next?.toDto()
)

private fun rawUri(id: UUID) = URI("$PhotoApiEndpoint/$id$RawPhotoEndpoint")

private fun compressedUri(id: UUID) = URI("$PhotoApiEndpoint/$id$CompressedPhotoEndpoint")
