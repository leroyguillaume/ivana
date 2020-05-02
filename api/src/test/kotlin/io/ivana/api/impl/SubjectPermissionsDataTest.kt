package io.ivana.api.impl

import java.util.*

internal class SubjectPermissionsDataTest : JsonTest(
    filename = "event-data/subject-permissions.json",
    expectedValue = SubjectPermissionsData(
        subjectId = UUID.fromString("a526a3f3-98dc-4cca-8d5b-43a20fe02963"),
        permissions = setOf(PermissionData.Read)
    ),
    deserializeAs = typeOf<SubjectPermissionsData>()
)
