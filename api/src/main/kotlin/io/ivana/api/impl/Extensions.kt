package io.ivana.api.impl

import io.ivana.core.*

internal fun EventSource.toData() = when (this) {
    is EventSource.System -> EventSourceData.System
    is EventSource.User -> EventSourceData.User(id, ip)
}

internal fun Permission.toData() = when (this) {
    Permission.Read -> PermissionData.Read
    Permission.Update -> PermissionData.Update
    Permission.Delete -> PermissionData.Delete
    Permission.UpdatePermissions -> PermissionData.UpdatePermissions
}

internal fun Photo.Type.toPhotoTypeData() = when (this) {
    Photo.Type.Jpg -> PhotoTypeData.Jpg
    Photo.Type.Png -> PhotoTypeData.Png
}

internal fun Role.toRoleData() = when (this) {
    Role.User -> RoleData.User
    Role.Admin -> RoleData.Admin
    Role.SuperAdmin -> RoleData.SuperAdmin
}

internal fun Set<UserPermissions>.toSubjectPermissionsSet() = map { userPerms ->
    SubjectPermissions(
        subjectId = userPerms.user.id,
        permissions = userPerms.permissions
    )
}.toSet()

internal fun SubjectPermissions.toData() = SubjectPermissionsData(
    subjectId = subjectId,
    permissions = permissions.map { it.toData() }.toSet()
)

internal fun SubjectPermissionsData.toSubjectPermissions() = SubjectPermissions(
    subjectId = subjectId,
    permissions = permissions.map { it.permission }.toSet()
)
