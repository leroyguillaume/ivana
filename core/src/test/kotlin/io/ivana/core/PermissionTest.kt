@file:Suppress("ClassName")

package io.ivana.core

import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PermissionTest {
    @Nested
    inner class fromLabel {
        @Test
        fun `should throw exception if label is unknown`() {
            val label = "label"
            val exception = assertThrows<IllegalArgumentException> { Permission.fromLabel(label) }
            exception shouldHaveMessage "Unknown permission '$label'"
        }

        @Test
        fun read() {
            Permission.fromLabel("read") shouldBe Permission.Read
        }

        @Test
        fun update() {
            Permission.fromLabel("update") shouldBe Permission.Update
        }

        @Test
        fun delete() {
            Permission.fromLabel("delete") shouldBe Permission.Delete
        }

        @Test
        fun updatePermissions() {
            Permission.fromLabel("update_permissions") shouldBe Permission.UpdatePermissions
        }
    }
}
