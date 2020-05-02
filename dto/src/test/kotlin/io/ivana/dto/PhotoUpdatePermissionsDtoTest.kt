package io.ivana.dto

import java.util.*

internal class PhotoUpdatePermissionsDtoTest : JsonTest(
    filename = "photo-permissions-update.json",
    expectedValue = PhotoUpdatePermissionsDto(
        permissionsToAdd = setOf(
            SubjectPermissionsUpdateDto(
                subjectId = UUID.fromString("a526a3f3-98dc-4cca-8d5b-43a20fe02963"),
                permissions = setOf(PermissionDto.Read)
            )
        ),
        permissionsToRemove = setOf(
            SubjectPermissionsUpdateDto(
                subjectId = UUID.fromString("faf7a32c-8211-4536-9a18-417a34aedf21"),
                permissions = setOf(PermissionDto.Delete)
            )
        )
    ),
    deserializeAs = typeOf<PhotoUpdatePermissionsDto>()
)
