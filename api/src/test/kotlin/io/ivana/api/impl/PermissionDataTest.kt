@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.Permission
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PermissionDataTest {
    @Nested
    inner class Read : JsonTest(
        filename = "event-data/permission/read.json",
        expectedValue = Wrapper(PermissionData.Read),
        deserializeAs = typeOf<Wrapper>()
    ) {
        private val typeData = PermissionData.Read
        private val type = Permission.Read
        private val sqlValue = "read"

        @Test
        fun type() {
            typeData.permission shouldBe type
        }

        @Test
        fun sqlValue() {
            typeData.sqlValue shouldBe sqlValue
        }
    }

    @Nested
    inner class Update : JsonTest(
        filename = "event-data/permission/update.json",
        expectedValue = Wrapper(PermissionData.Update),
        deserializeAs = typeOf<Wrapper>()
    ) {
        private val typeData = PermissionData.Update
        private val type = Permission.Update
        private val sqlValue = "update"

        @Test
        fun type() {
            typeData.permission shouldBe type
        }

        @Test
        fun sqlValue() {
            typeData.sqlValue shouldBe sqlValue
        }
    }

    @Nested
    inner class Delete : JsonTest(
        filename = "event-data/permission/delete.json",
        expectedValue = Wrapper(PermissionData.Delete),
        deserializeAs = typeOf<Wrapper>()
    ) {
        private val typeData = PermissionData.Delete
        private val type = Permission.Delete
        private val sqlValue = "delete"

        @Test
        fun type() {
            typeData.permission shouldBe type
        }

        @Test
        fun sqlValue() {
            typeData.sqlValue shouldBe sqlValue
        }
    }

    @Nested
    inner class UpdatePermissions : JsonTest(
        filename = "event-data/permission/update-permissions.json",
        expectedValue = Wrapper(PermissionData.UpdatePermissions),
        deserializeAs = typeOf<Wrapper>()
    ) {
        private val typeData = PermissionData.UpdatePermissions
        private val type = Permission.UpdatePermissions
        private val sqlValue = "update_permissions"

        @Test
        fun type() {
            typeData.permission shouldBe type
        }

        @Test
        fun sqlValue() {
            typeData.sqlValue shouldBe sqlValue
        }
    }

    private data class Wrapper(
        val permission: PermissionData
    )
}
