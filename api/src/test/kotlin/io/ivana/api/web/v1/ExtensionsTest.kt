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
    fun albumToDto() {
        val album = Album(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            name = "album",
            creationDate = OffsetDateTime.now()
        )
        val dto = AlbumDto(
            id = album.id,
            name = album.name,
            creationDate = album.creationDate
        )
        album.toDto() shouldBe dto
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
                    no = 1
                ),
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = UUID.randomUUID(),
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Png,
                    hash = "hash2",
                    no = 2
                )
            ),
            no = 1,
            totalItems = 10,
            totalPages = 100
        )
        val dto = PageDto(
            content = page.content.map { it.toSimpleDto() },
            no = page.no,
            totalItems = page.totalItems,
            totalPages = page.totalPages
        )
        page.toDto { it.toSimpleDto() } shouldBe dto
    }

    @Test
    fun photoToSimpleDtoTest() {
        val photo = Photo(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1
        )
        val dto = PhotoDto.Simple(
            id = photo.id,
            rawUri = rawUri(photo.id),
            compressedUri = compressedUri(photo.id)
        )
        photo.toSimpleDto() shouldBe dto
    }

    @Test
    fun linkedPhotosToNavigableDto() {
        val linkedPhotos = LinkedPhotos(
            current = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash2",
                no = 2
            ),
            previous = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash1",
                no = 1
            ),
            next = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 3
            )
        )
        val dto = PhotoDto.Navigable(
            id = linkedPhotos.current.id,
            rawUri = rawUri(linkedPhotos.current.id),
            compressedUri = compressedUri(linkedPhotos.current.id),
            previous = PhotoDto.Simple(
                id = linkedPhotos.previous!!.id,
                rawUri = rawUri(linkedPhotos.previous!!.id),
                compressedUri = compressedUri(linkedPhotos.previous!!.id)
            ),
            next = PhotoDto.Simple(
                id = linkedPhotos.next!!.id,
                rawUri = rawUri(linkedPhotos.next!!.id),
                compressedUri = compressedUri(linkedPhotos.next!!.id)
            )
        )
        linkedPhotos.toNavigableDto() shouldBe dto
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
