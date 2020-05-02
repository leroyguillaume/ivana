package io.ivana.dto

import org.junit.jupiter.api.Nested

internal class PermissionDtoTest {
    @Nested
    inner class Read : JsonTest(
        filename = "permission/read.json",
        expectedValue = Wrapper(PermissionDto.Read),
        deserializeAs = typeOf<Wrapper>()
    )

    @Nested
    inner class Update : JsonTest(
        filename = "permission/update.json",
        expectedValue = Wrapper(PermissionDto.Update),
        deserializeAs = typeOf<Wrapper>()
    )

    @Nested
    inner class Delete : JsonTest(
        filename = "permission/delete.json",
        expectedValue = Wrapper(PermissionDto.Delete),
        deserializeAs = typeOf<Wrapper>()
    )

    @Nested
    inner class UpdatePermissions : JsonTest(
        filename = "permission/update-permissions.json",
        expectedValue = Wrapper(PermissionDto.UpdatePermissions),
        deserializeAs = typeOf<Wrapper>()
    )

    private data class Wrapper(
        val permission: PermissionDto
    )
}
