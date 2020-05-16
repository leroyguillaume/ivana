@file:Suppress("ClassName")

package io.ivana.api.web.v1

import io.ivana.core.*
import io.ivana.dto.*
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

internal class ExtensionsTest {
    @Test
    fun albumToCompleteDto() {
        val perms = setOf(Permission.Read)
        val album = Album(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            name = "album",
            creationDate = OffsetDateTime.now()
        )
        val dto = AlbumDto.Complete(
            id = album.id,
            name = album.name,
            ownerId = album.ownerId,
            creationDate = album.creationDate,
            permissions = perms.map { it.toDto() }.toSet()
        )
        album.toCompleteDto(perms) shouldBe dto
    }

    @Test
    fun albumToLightDto() {
        val album = Album(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            name = "album",
            creationDate = OffsetDateTime.now()
        )
        val dto = AlbumDto.Light(
            id = album.id,
            name = album.name,
            ownerId = album.ownerId,
            creationDate = album.creationDate
        )
        album.toLightDto() shouldBe dto
    }

    @Test
    fun pageToDto() {
        val page = Page(
            content = listOf(
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = UUID.randomUUID(),
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash1",
                    no = 1,
                    version = 1
                ),
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = UUID.randomUUID(),
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Png,
                    hash = "hash2",
                    no = 2,
                    version = 1
                )
            ),
            no = 1,
            totalItems = 10,
            totalPages = 100
        )
        val dto = PageDto(
            content = page.content.map { it.toLightDto() },
            no = page.no,
            totalItems = page.totalItems,
            totalPages = page.totalPages
        )
        page.toDto { it.toLightDto() } shouldBe dto
    }

    @Test
    fun photoToLightDtoTest() {
        val photo = Photo(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1,
            version = 1
        )
        val dto = PhotoDto.Light(
            id = photo.id,
            ownerId = photo.ownerId,
            rawUri = rawUri(photo.id),
            compressedUri = compressedUri(photo.id)
        )
        photo.toLightDto() shouldBe dto
    }

    @Test
    fun linkedPhotosToNavigableDto() {
        val perms = setOf(Permission.Read)
        val linkedPhotos = LinkedPhotos(
            current = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash2",
                no = 2,
                version = 1
            ),
            previous = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash1",
                no = 1,
                version = 1
            ),
            next = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 3,
                version = 1
            )
        )
        val dto = PhotoDto.Complete.Navigable(
            id = linkedPhotos.current.id,
            ownerId = linkedPhotos.current.ownerId,
            rawUri = rawUri(linkedPhotos.current.id),
            compressedUri = compressedUri(linkedPhotos.current.id),
            permissions = perms.map { it.toDto() }.toSet(),
            previous = PhotoDto.Light(
                id = linkedPhotos.previous!!.id,
                ownerId = linkedPhotos.previous!!.ownerId,
                rawUri = rawUri(linkedPhotos.previous!!.id),
                compressedUri = compressedUri(linkedPhotos.previous!!.id)
            ),
            next = PhotoDto.Light(
                id = linkedPhotos.next!!.id,
                ownerId = linkedPhotos.next!!.ownerId,
                rawUri = rawUri(linkedPhotos.next!!.id),
                compressedUri = compressedUri(linkedPhotos.next!!.id)
            )
        )
        linkedPhotos.toNavigableDto(perms) shouldBe dto
    }

    @Test
    fun photoToSimpleDtoTest() {
        val perms = setOf(Permission.Read)
        val photo = Photo(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1,
            version = 1
        )
        val dto = PhotoDto.Complete.Simple(
            id = photo.id,
            ownerId = photo.ownerId,
            rawUri = rawUri(photo.id),
            compressedUri = compressedUri(photo.id),
            permissions = perms.map { it.toDto() }.toSet()
        )
        photo.toSimpleDto(perms) shouldBe dto
    }

    @Nested
    inner class permissionToPermissionDto {
        @Test
        fun read() {
            Permission.Read.toDto() shouldBe PermissionDto.Read
        }

        @Test
        fun update() {
            Permission.Update.toDto() shouldBe PermissionDto.Update
        }

        @Test
        fun delete() {
            Permission.Delete.toDto() shouldBe PermissionDto.Delete
        }

        @Test
        fun updatePermissions() {
            Permission.UpdatePermissions.toDto() shouldBe PermissionDto.UpdatePermissions
        }
    }

    @Nested
    inner class permissionDtoToPermission {
        @Test
        fun read() {
            PermissionDto.Read.toPermission() shouldBe Permission.Read
        }

        @Test
        fun update() {
            PermissionDto.Update.toPermission() shouldBe Permission.Update
        }

        @Test
        fun delete() {
            PermissionDto.Delete.toPermission() shouldBe Permission.Delete
        }

        @Test
        fun updatePermissions() {
            PermissionDto.UpdatePermissions.toPermission() shouldBe Permission.UpdatePermissions
        }
    }

    @Nested
    inner class roleToRoleDto {
        @Test
        fun user() {
            Role.User.toDto() shouldBe RoleDto.User
        }

        @Test
        fun admin() {
            Role.Admin.toDto() shouldBe RoleDto.Admin
        }

        @Test
        fun super_admin() {
            Role.SuperAdmin.toDto() shouldBe RoleDto.SuperAdmin
        }
    }

    @Test
    fun setOfSubjectPermissionsUpdateDtoToSetOfUserPermissions() {
        val user = User(
            id = UUID.randomUUID(),
            name = "user",
            creationDate = OffsetDateTime.now(),
            hashedPwd = "hashedPwd",
            role = Role.User
        )
        val perms = setOf(Permission.Read, Permission.Delete)
        val users = mapOf(user.id to user)
        val subjPermsUpdateDtos = setOf(
            SubjectPermissionsUpdateDto(
                subjectId = user.id,
                permissions = perms.map { it.toDto() }.toSet()
            )
        )
        val usersPerms = setOf(
            UserPermissions(
                user = user,
                permissions = perms
            )
        )
        subjPermsUpdateDtos.toUserPermissionsSet(users) shouldBe usersPerms
    }

    @Test
    fun subjectPermissionsToDto() {
        val subjPerms = SubjectPermissions(
            subjectId = UUID.randomUUID(),
            permissions = setOf(Permission.Read)
        )
        val dto = SubjectPermissionsDto(
            subjectId = subjPerms.subjectId,
            subjectName = "user",
            permissions = setOf(PermissionDto.Read)
        )
        subjPerms.toDto(dto.subjectName) shouldBe dto
    }

    @Test
    fun userToDto() {
        val user = User(
            id = UUID.randomUUID(),
            name = "admin",
            hashedPwd = "hashedPwd",
            role = Role.SuperAdmin,
            creationDate = OffsetDateTime.now()
        )
        val dto = UserDto(
            id = user.id,
            name = user.name,
            role = user.role.toDto(),
            creationDate = user.creationDate
        )
        user.toDto() shouldBe dto
    }

    private fun rawUri(id: UUID) = URI("$PhotoApiEndpoint/$id$RawPhotoEndpoint")

    private fun compressedUri(id: UUID) = URI("$PhotoApiEndpoint/$id$CompressedPhotoEndpoint")
}
