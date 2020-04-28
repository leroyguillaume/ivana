package io.ivana.api.impl

import io.ivana.core.EventSource
import io.ivana.core.Photo
import io.ivana.core.Role

internal fun EventSource.toData() = when (this) {
    is EventSource.System -> EventSourceData.System
    is EventSource.User -> EventSourceData.User(id, ip)
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
