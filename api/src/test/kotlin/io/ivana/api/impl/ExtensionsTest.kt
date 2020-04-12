@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.EventSource
import io.ivana.core.Photo
import io.ivana.core.Role
import io.ivana.core.Transform
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.util.*

internal class ExtensionsTest {
    @Nested
    inner class eventSourceToDataTest {
        @Test
        fun system() {
            EventSource.System.toData() shouldBe EventSourceData.System
        }

        @Test
        fun user() {
            val id = UUID.randomUUID()
            val ip = InetAddress.getByName("127.0.0.1")
            EventSource.User(id, ip).toData() shouldBe EventSourceData.User(id, ip)
        }
    }

    @Nested
    inner class photoTypeToPhotoTypeDataTest {
        @Test
        fun jpg() {
            Photo.Type.Jpg.toPhotoTypeData() shouldBe PhotoTypeData.Jpg
        }

        @Test
        fun png() {
            Photo.Type.Png.toPhotoTypeData() shouldBe PhotoTypeData.Png
        }
    }

    @Nested
    inner class roletoRoleDataTest {
        @Test
        fun user() {
            Role.User.toRoleData() shouldBe RoleData.User
        }

        @Test
        fun admin() {
            Role.Admin.toRoleData() shouldBe RoleData.Admin
        }

        @Test
        fun super_admin() {
            Role.SuperAdmin.toRoleData() shouldBe RoleData.SuperAdmin
        }
    }

    @Nested
    inner class directionToDirectionData {
        @Test
        fun clockwise() {
            Transform.Rotation.Direction.Clockwise.toDirectionData().shouldBe(
                PhotoEventData.Transform.Content.Rotation.Direction.Clockwise
            )
        }

        @Test
        fun counterclockwise() {
            Transform.Rotation.Direction.Counterclockwise.toDirectionData().shouldBe(
                PhotoEventData.Transform.Content.Rotation.Direction.Counterclockwise
            )
        }
    }
}
