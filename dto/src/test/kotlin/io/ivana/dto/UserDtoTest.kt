package io.ivana.dto

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

internal class UserDtoTest : JsonTest(
    filename = "user.json",
    expectedValue = UserDto(
        id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
        name = "admin",
        role = RoleDto.SuperAdmin,
        creationDate = OffsetDateTime.of(2020, 4, 13, 14, 55, 0, 0, ZoneOffset.UTC)
    ),
    deserializeAs = typeOf<UserDto>()
)
