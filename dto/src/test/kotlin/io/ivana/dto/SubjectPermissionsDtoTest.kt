package io.ivana.dto

import java.util.*

internal class SubjectPermissionsDtoTest : JsonTest(
    filename = "subject-permissions.json",
    expectedValue = SubjectPermissionsDto(
        subjectId = UUID.fromString("faf7a32c-8211-4536-9a18-417a34aedf21"),
        subjectName = "user",
        permissions = setOf(PermissionDto.Delete)
    ),
    deserializeAs = typeOf<SubjectPermissionsDto>()
)
