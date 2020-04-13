package io.ivana.api.web.v1

import io.ivana.api.web.RootApiEndpoint
import io.ivana.core.*
import io.ivana.dto.*
import java.net.URI
import java.util.*

const val RootEndpoint = "$RootApiEndpoint/v1"

const val LoginEndpoint = "$RootEndpoint/login"
const val LogoutEndpoint = "$RootEndpoint/logout"
const val UserApiEndpoint = "$RootEndpoint/user"
const val PasswordUpdateEndpoint = "/password"
const val PhotoApiEndpoint = "$RootEndpoint/photo"
const val RawPhotoEndpoint = "/raw"
const val CompressedPhotoEndpoint = "/compressed"
const val TransformPhotoEndpoint = "/transform"

const val FilesParamName = "files"
const val NavigableParamName = "navigable"
const val PageParamName = "page"
const val SizeParamName = "size"

const val UuidRegex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"

fun <E : Entity, D : EntityDto> Page<E>.toDto(mapper: (E) -> D) = PageDto(
    content = content.map(mapper),
    no = no,
    totalItems = totalItems,
    totalPages = totalPages
)

fun Photo.toSimpleDto() = PhotoDto.Simple(
    id = id,
    rawUri = rawUri(id),
    compressedUri = compressedUri(id)
)

fun LinkedPhotos.toNavigableDto() = PhotoDto.Navigable(
    id = current.id,
    rawUri = rawUri(current.id),
    compressedUri = compressedUri(current.id),
    previous = previous?.toSimpleDto(),
    next = next?.toSimpleDto()
)

fun Role.toDto() = when (this) {
    Role.User -> RoleDto.User
    Role.Admin -> RoleDto.Admin
    Role.SuperAdmin -> RoleDto.SuperAdmin
}

fun User.toDto() = UserDto(
    id = id,
    name = name,
    role = role.toDto(),
    creationDate = creationDate
)

private fun rawUri(id: UUID) = URI("$PhotoApiEndpoint/$id$RawPhotoEndpoint")

private fun compressedUri(id: UUID) = URI("$PhotoApiEndpoint/$id$CompressedPhotoEndpoint")
