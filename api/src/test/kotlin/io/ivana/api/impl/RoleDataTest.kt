@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.Role
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class RoleDataTest {
    @Nested
    inner class user : JsonTest(
        filename = "event-data/role/user.json",
        expectedValue = Wrapper(RoleData.User),
        deserializeAs = typeOf<Wrapper>()
    ) {
        private val roleData = RoleData.User
        private val role = Role.User
        private val sqlValue = "user"

        @Test
        fun type() {
            roleData.role shouldBe role
        }

        @Test
        fun sqlValue() {
            roleData.sqlValue shouldBe sqlValue
        }
    }

    @Nested
    inner class admin : JsonTest(
        filename = "event-data/role/admin.json",
        expectedValue = Wrapper(RoleData.Admin),
        deserializeAs = typeOf<Wrapper>()
    ) {
        private val roleData = RoleData.Admin
        private val role = Role.Admin
        private val sqlValue = "admin"

        @Test
        fun type() {
            roleData.role shouldBe role
        }

        @Test
        fun sqlValue() {
            roleData.sqlValue shouldBe sqlValue
        }
    }

    @Nested
    inner class super_admin : JsonTest(
        filename = "event-data/role/super-admin.json",
        expectedValue = Wrapper(RoleData.SuperAdmin),
        deserializeAs = typeOf<Wrapper>()
    ) {
        private val roleData = RoleData.SuperAdmin
        private val role = Role.SuperAdmin
        private val sqlValue = "super_admin"

        @Test
        fun type() {
            roleData.role shouldBe role
        }

        @Test
        fun sqlValue() {
            roleData.sqlValue shouldBe sqlValue
        }
    }

    private data class Wrapper(
        val role: RoleData
    )
}
