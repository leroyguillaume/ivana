package io.ivana.dto

import java.util.*

internal class SubjectPermissionsUpdateDtoTest : JsonTest(
    filename = "subject-permissions-update.json",
    expectedValue = SubjectPermissionsUpdateDto(
        subjectId = UUID.fromString("faf7a32c-8211-4536-9a18-417a34aedf21"),
        permissions = setOf(PermissionDto.Delete)
    ),
    deserializeAs = typeOf<SubjectPermissionsUpdateDto>()
)
