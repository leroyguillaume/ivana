@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.time.OffsetDateTime
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
    inner class permissionToData {
        @Test
        fun read() {
            Permission.Read.toData() shouldBe PermissionData.Read
        }

        @Test
        fun update() {
            Permission.Read.toData() shouldBe PermissionData.Read
        }

        @Test
        fun delete() {
            Permission.Delete.toData() shouldBe PermissionData.Delete
        }

        @Test
        fun updatePermissions() {
            Permission.UpdatePermissions.toData() shouldBe PermissionData.UpdatePermissions
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
    inner class roleToRoleDataTest {
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

    @Test
    fun setOfUserPermissionsToSetOfSubjectPermissions() {
        val userId = UUID.randomUUID()
        val perms = setOf(Permission.Read, Permission.Delete)
        val usersPerms = setOf(
            UserPermissions(
                user = User(
                    id = userId,
                    name = "user",
                    creationDate = OffsetDateTime.now(),
                    hashedPwd = "hashedPwd",
                    role = Role.User
                ),
                permissions = perms
            )
        )
        val subjsPerms = setOf(
            SubjectPermissions(
                subjectId = userId,
                permissions = perms
            )
        )
        usersPerms.toSubjectPermissionsSet() shouldBe subjsPerms
    }

    @Test
    fun subjectPermissionsToData() {
        val subjPerms = SubjectPermissions(
            subjectId = UUID.randomUUID(),
            permissions = setOf(Permission.Read)
        )
        val subjPermsData = SubjectPermissionsData(
            subjectId = subjPerms.subjectId,
            permissions = setOf(PermissionData.Read)
        )
        subjPerms.toData() shouldBe subjPermsData
    }

    @Test
    fun subjectPermissionsDataToSubjectPermissions() {
        val subjPermsData = SubjectPermissionsData(
            subjectId = UUID.randomUUID(),
            permissions = setOf(PermissionData.Read)
        )
        val subjPerms = SubjectPermissions(
            subjectId = subjPermsData.subjectId,
            permissions = setOf(Permission.Read)
        )
        subjPermsData.toSubjectPermissions() shouldBe subjPerms
    }
}
