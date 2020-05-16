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
const val MeEndpoint = "/me"
const val PasswordUpdateEndpoint = "/password"
const val PhotoApiEndpoint = "$RootEndpoint/photo"
const val RawPhotoEndpoint = "/raw"
const val CompressedPhotoEndpoint = "/compressed"
const val TransformPhotoEndpoint = "/transform"
const val AlbumApiEndpoint = "$RootEndpoint/album"
const val ContentEndpoint = "/content"
const val PermissionsEndpoint = "/permissions"

const val FilesParamName = "files"
const val NavigableParamName = "navigable"
const val PageParamName = "page"
const val SizeParamName = "size"

const val UuidRegex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"

fun Album.toDto() = AlbumDto(
    id = id,
    name = name,
    ownerId = ownerId,
    creationDate = creationDate
)

fun LinkedPhotos.toNavigableDto(permissions: Set<Permission>) = PhotoDto.Complete.Navigable(
    id = current.id,
    ownerId = current.ownerId,
    rawUri = rawUri(current.id),
    compressedUri = compressedUri(current.id),
    permissions = permissions.map { it.toDto() }.toSet(),
    previous = previous?.toLightDto(),
    next = next?.toLightDto()
)

fun <E, D> Page<E>.toDto(mapper: (E) -> D) = PageDto(
    content = content.map(mapper),
    no = no,
    totalItems = totalItems,
    totalPages = totalPages
)

fun Permission.toDto() = when (this) {
    Permission.Read -> PermissionDto.Read
    Permission.Update -> PermissionDto.Update
    Permission.Delete -> PermissionDto.Delete
    Permission.UpdatePermissions -> PermissionDto.UpdatePermissions
}

fun Photo.toLightDto() = PhotoDto.Light(
    id = id,
    ownerId = ownerId,
    rawUri = rawUri(id),
    compressedUri = compressedUri(id)
)

fun Photo.toSimpleDto(permissions: Set<Permission>) = PhotoDto.Complete.Simple(
    id = id,
    ownerId = ownerId,
    rawUri = rawUri(id),
    compressedUri = compressedUri(id),
    permissions = permissions.map { it.toDto() }.toSet()
)

fun Role.toDto() = when (this) {
    Role.User -> RoleDto.User
    Role.Admin -> RoleDto.Admin
    Role.SuperAdmin -> RoleDto.SuperAdmin
}

fun SubjectPermissions.toDto(name: String) = SubjectPermissionsDto(
    subjectId = subjectId,
    subjectName = name,
    permissions = permissions.map { it.toDto() }.toSet()
)

fun User.toDto() = UserDto(
    id = id,
    name = name,
    role = role.toDto(),
    creationDate = creationDate
)

private fun rawUri(id: UUID) = URI("$PhotoApiEndpoint/$id$RawPhotoEndpoint")

private fun compressedUri(id: UUID) = URI("$PhotoApiEndpoint/$id$CompressedPhotoEndpoint")
