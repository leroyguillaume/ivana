package io.ivana.dto

internal class UserCreationDtoTest : JsonTest(
    filename = "user-creation.json",
    expectedValue = UserCreationDto(
        name = "admin",
        pwd = "changeit",
        role = RoleDto.SuperAdmin
    ),
    deserializeAs = typeOf<UserCreationDto>()
)
