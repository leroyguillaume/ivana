package io.ivana.dto

import org.junit.jupiter.api.Nested

internal class RoleDtoTest {
    @Nested
    inner class User : JsonTest(
        filename = "role/user.json",
        expectedValue = Wrapper(RoleDto.User),
        deserializeAs = typeOf<Wrapper>()
    )

    @Nested
    inner class Admin : JsonTest(
        filename = "role/admin.json",
        expectedValue = Wrapper(RoleDto.Admin),
        deserializeAs = typeOf<Wrapper>()
    )

    @Nested
    inner class SuperAdmin : JsonTest(
        filename = "role/super-admin.json",
        expectedValue = Wrapper(RoleDto.SuperAdmin),
        deserializeAs = typeOf<Wrapper>()
    )

    private data class Wrapper(
        val role: RoleDto
    )
}
