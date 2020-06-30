@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*

internal class AlbumServiceImplTest {
    private lateinit var albumRepo: AlbumRepository
    private lateinit var userRepo: UserRepository
    private lateinit var authzRepo: UserAlbumAuthorizationRepository
    private lateinit var albumEventRepo: AlbumEventRepository
    private lateinit var photoRepo: PhotoRepository
    private lateinit var service: AlbumServiceImpl

    @BeforeEach
    fun beforeEach() {
        albumRepo = mockk()
        userRepo = mockk()
        authzRepo = mockk()
        albumEventRepo = mockk()
        photoRepo = mockk()

        service = AlbumServiceImpl(albumRepo, userRepo, authzRepo, albumEventRepo, photoRepo)
    }

    @Nested
    inner class create {
        private val creationEvent = AlbumEvent.Creation(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            content = AlbumEvent.Creation.Content(name = "album")
        )
        private val expectedAlbum = Album(
            id = creationEvent.subjectId,
            ownerId = creationEvent.source.id,
            name = creationEvent.content.name,
            creationDate = creationEvent.date
        )

        @Test
        fun `should save creation event`() {
            every {
                albumEventRepo.saveCreationEvent(creationEvent.content.name, creationEvent.source)
            } returns creationEvent
            every { albumRepo.fetchById(expectedAlbum.id) } returns expectedAlbum
            val album = service.create(creationEvent.content.name, creationEvent.source)
            album shouldBe expectedAlbum
            verify { albumEventRepo.saveCreationEvent(creationEvent.content.name, creationEvent.source) }
            verify { albumRepo.fetchById(expectedAlbum.id) }
            confirmVerified(albumRepo, albumEventRepo)
        }
    }

    @Nested
    inner class delete {
        private val event = AlbumEvent.Deletion(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1"))
        )

        @Test
        fun `should throw exception if album does not exist`() {
            every { albumRepo.existsById(event.subjectId) } returns false
            val exception = assertThrows<EntityNotFoundException> { service.delete(event.subjectId, event.source) }
            exception shouldHaveMessage "Album ${event.subjectId} does not exist"
            verify { albumRepo.existsById(event.subjectId) }
            confirmVerified(albumRepo)
        }

        @Test
        fun `should delete album`() {
            every { albumRepo.existsById(event.subjectId) } returns true
            every { albumEventRepo.saveDeletionEvent(event.subjectId, event.source) } returns event
            service.delete(event.subjectId, event.source)
            verify { albumRepo.existsById(event.subjectId) }
            verify { albumEventRepo.saveDeletionEvent(event.subjectId, event.source) }
            confirmVerified(albumRepo)
        }
    }

    @Nested
    inner class getAll {
        private val pageNo = 1
        private val pageSize = 3
        private val expectedPage = Page(
            content = listOf(
                Album(
                    id = UUID.randomUUID(),
                    ownerId = UUID.randomUUID(),
                    name = "album1",
                    creationDate = OffsetDateTime.now()
                ),
                Album(
                    id = UUID.randomUUID(),
                    ownerId = UUID.randomUUID(),
                    name = "album2",
                    creationDate = OffsetDateTime.now()
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )

        @Test
        fun `should return page`() {
            every { albumRepo.fetchAll(pageNo - 1, pageSize) } returns expectedPage.content
            every { albumRepo.count() } returns 2
            val page = service.getAll(pageNo, pageSize)
            page shouldBe expectedPage
            verify { albumRepo.fetchAll(pageNo - 1, pageSize) }
            verify { albumRepo.count() }
            confirmVerified(albumRepo)
        }
    }

    @Nested
    inner class `getAll with owner id` {
        private val ownerId = UUID.randomUUID()
        private val pageNo = 1
        private val pageSize = 3
        private val expectedPage = Page(
            content = listOf(
                Album(
                    id = UUID.randomUUID(),
                    ownerId = UUID.randomUUID(),
                    name = "album1",
                    creationDate = OffsetDateTime.now()
                ),
                Album(
                    id = UUID.randomUUID(),
                    ownerId = UUID.randomUUID(),
                    name = "album2",
                    creationDate = OffsetDateTime.now()
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )

        @Test
        fun `should return page`() {
            every { albumRepo.fetchAll(ownerId, pageNo - 1, pageSize) } returns expectedPage.content
            every { albumRepo.count(ownerId) } returns 2
            val page = service.getAll(ownerId, pageNo, pageSize)
            page shouldBe expectedPage
            verify { albumRepo.fetchAll(ownerId, pageNo - 1, pageSize) }
            verify { albumRepo.count(ownerId) }
            confirmVerified(albumRepo)
        }
    }

    @Nested
    inner class getAllByIds {
        private val expectedAlbums = setOf(
            Album(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                name = "album1",
                creationDate = OffsetDateTime.now()
            ),
            Album(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                name = "album2",
                creationDate = OffsetDateTime.now()
            )
        )
        private val ids = expectedAlbums.map { it.id }.toSet()

        @Test
        fun `should throw exception if entities not found`() {
            every { albumRepo.fetchAllByIds(ids) } returns emptySet()
            val exception = assertThrows<ResourcesNotFoundException> { service.getAllByIds(ids) }
            exception.ids shouldBe ids
            verify { albumRepo.fetchAllByIds(ids) }
            confirmVerified(albumRepo)
        }

        @Test
        fun `should return all albums`() {
            every { albumRepo.fetchAllByIds(ids) } returns expectedAlbums
            val albums = service.getAllByIds(ids)
            albums shouldBe expectedAlbums
            verify { albumRepo.fetchAllByIds(ids) }
            confirmVerified(albumRepo)
        }
    }

    @Nested
    inner class getAllPermissions {
        private val albumId = UUID.randomUUID()
        private val pageNo = 1
        private val pageSize = 3
        private val expectedPage = Page(
            content = listOf(
                SubjectPermissions(
                    subjectId = UUID.randomUUID(),
                    permissions = setOf(Permission.Read)
                ),
                SubjectPermissions(
                    subjectId = UUID.randomUUID(),
                    permissions = setOf(Permission.Update)
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )

        @Test
        fun `should return page`() {
            every { authzRepo.fetchAll(albumId, pageNo - 1, pageSize) } returns expectedPage.content
            every { authzRepo.count(albumId) } returns expectedPage.totalItems
            val page = service.getAllPermissions(albumId, pageNo, pageSize)
            page shouldBe expectedPage
            verify { authzRepo.fetchAll(albumId, pageNo - 1, pageSize) }
            verify { authzRepo.count(albumId) }
            confirmVerified(authzRepo)
        }
    }

    @Nested
    inner class getPermissions {
        private val userId = UUID.randomUUID()
        private val albumId = UUID.randomUUID()
        private val permissions = setOf(Permission.Read)

        @Test
        fun `should throw exception if album does not exist`() {
            every { albumRepo.existsById(albumId) } returns false
            val exception = assertThrows<EntityNotFoundException> { service.getPermissions(albumId, userId) }
            exception shouldHaveMessage "Album $albumId does not exist"
            verify { albumRepo.existsById(albumId) }
            confirmVerified(albumRepo)
        }

        @Test
        fun `should throw exception if user does not exist`() {
            every { albumRepo.existsById(albumId) } returns true
            every { userRepo.existsById(userId) } returns false
            val exception = assertThrows<EntityNotFoundException> { service.getPermissions(albumId, userId) }
            exception shouldHaveMessage "User $userId does not exist"
            verify { albumRepo.existsById(albumId) }
            verify { userRepo.existsById(userId) }
            confirmVerified(albumRepo, userRepo)
        }

        @Test
        fun `should returns empty set if no permissions defined`() {
            every { albumRepo.existsById(albumId) } returns true
            every { userRepo.existsById(userId) } returns true
            every { authzRepo.fetch(userId, albumId) } returns null
            val perms = service.getPermissions(albumId, userId)
            perms.shouldBeEmpty()
            verify { albumRepo.existsById(albumId) }
            verify { userRepo.existsById(userId) }
            verify { authzRepo.fetch(userId, albumId) }
            confirmVerified(albumRepo, userRepo, authzRepo)
        }

        @Test
        fun `should return permissions`() {
            every { albumRepo.existsById(albumId) } returns true
            every { userRepo.existsById(userId) } returns true
            every { authzRepo.fetch(userId, albumId) } returns permissions
            val perms = service.getPermissions(albumId, userId)
            perms shouldBe permissions
            verify { albumRepo.existsById(albumId) }
            verify { userRepo.existsById(userId) }
            verify { authzRepo.fetch(userId, albumId) }
            confirmVerified(albumRepo, userRepo, authzRepo)
        }
    }

    @Nested
    inner class getAllPhotos {
        private val albumId = UUID.randomUUID()
        private val userId = UUID.randomUUID()
        private val pageNo = 1
        private val pageSize = 3
        private val expectedPage = Page(
            content = listOf(
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = userId,
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash1",
                    no = 1,
                    version = 1
                ),
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = userId,
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash2",
                    no = 2,
                    version = 1
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )

        @Test
        fun `should throw exception if album does not exist`() {
            every { albumRepo.fetchSize(albumId, userId) } returns null
            val exception = assertThrows<EntityNotFoundException> {
                service.getAllPhotos(albumId, userId, pageNo, pageSize)
            }
            exception shouldHaveMessage "Album $albumId does not exist"
            verify { albumRepo.fetchSize(albumId, userId) }
            confirmVerified(albumRepo)
        }

        @Test
        fun `should return page`() {
            every { albumRepo.fetchSize(albumId, userId) } returns expectedPage.totalItems
            every { photoRepo.fetchAllOfAlbum(albumId, userId, pageNo - 1, pageSize) } returns expectedPage.content
            val page = service.getAllPhotos(albumId, userId, pageNo, pageSize)
            page shouldBe expectedPage
            verify { albumRepo.fetchSize(albumId, userId) }
            verify { photoRepo.fetchAllOfAlbum(albumId, userId, pageNo - 1, pageSize) }
            confirmVerified(albumRepo, photoRepo)
        }
    }

    @Nested
    inner class getById {
        private val expectedAlbum = Album(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            name = "album",
            creationDate = OffsetDateTime.now()
        )

        @Test
        fun `should throw exception if album does not exist`() {
            every { albumRepo.fetchById(expectedAlbum.id) } returns null
            val exception = assertThrows<EntityNotFoundException> { service.getById(expectedAlbum.id) }
            exception shouldHaveMessage "Album ${expectedAlbum.id} does not exist"
            verify { albumRepo.fetchById(expectedAlbum.id) }
            confirmVerified(albumRepo)
        }

        @Test
        fun `should return album with id`() {
            every { albumRepo.fetchById(expectedAlbum.id) } returns expectedAlbum
            val album = service.getById(expectedAlbum.id)
            album shouldBe expectedAlbum
            verify { albumRepo.fetchById(album.id) }
            confirmVerified(albumRepo)
        }
    }

    @Nested
    inner class suggest {
        private val expectedAlbums = listOf(
            Album(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                name = "album1",
                creationDate = OffsetDateTime.now()
            ),
            Album(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                name = "album2",
                creationDate = OffsetDateTime.now()
            )
        )
        private val name = "album"
        private val count = 2
        private val userId = UUID.randomUUID()
        private val perm = Permission.Read

        @Test
        fun `should return suggested albums`() {
            every { albumRepo.suggest(name, count, userId, perm) } returns expectedAlbums
            val albums = service.suggest(name, count, userId, perm)
            albums shouldBe expectedAlbums
            verify { albumRepo.suggest(name, count, userId, perm) }
            confirmVerified(albumRepo)
        }
    }

    @Nested
    inner class update {
        private val event = AlbumEvent.Update(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            content = AlbumEvent.Update.Content(
                name = "album",
                photosToAdd = listOf(UUID.randomUUID()),
                photosToRemove = listOf(UUID.randomUUID())
            )
        )
        private val emptyEvent = event.copy(
            content = event.content.copy(
                photosToAdd = emptyList(),
                photosToRemove = emptyList()
            )
        )
        private val duplicateIds = event.content.photosToAdd.toSet()
        private val expectedAlbum = Album(
            id = event.subjectId,
            ownerId = event.source.id,
            name = event.content.name,
            creationDate = OffsetDateTime.now()
        )

        @Test
        fun `should throw exception if album does not exist`() {
            every { albumRepo.existsById(expectedAlbum.id) } returns false
            val exception = assertThrows<EntityNotFoundException> {
                service.update(expectedAlbum.id, event.content, event.source)
            }
            exception shouldHaveMessage "Album ${expectedAlbum.id} does not exist"
            verify { albumRepo.existsById(expectedAlbum.id) }
            confirmVerified(albumRepo)
        }

        @Test
        fun `should throw exception if album already contains photos`() {
            every { albumRepo.existsById(expectedAlbum.id) } returns true
            every { albumRepo.fetchDuplicateIds(expectedAlbum.id, duplicateIds) } returns duplicateIds
            val exception = assertThrows<PhotosAlreadyInAlbumException> {
                service.update(expectedAlbum.id, event.content, event.source)
            }
            exception.photosIds shouldBe duplicateIds
            verify { albumRepo.existsById(expectedAlbum.id) }
            verify { albumRepo.fetchDuplicateIds(expectedAlbum.id, duplicateIds) }
            confirmVerified(albumRepo, photoRepo)
        }

        @Test
        fun `should return updated album`() {
            every { albumRepo.existsById(expectedAlbum.id) } returns true
            every { albumRepo.fetchDuplicateIds(expectedAlbum.id, duplicateIds) } returns emptySet()
            every { albumEventRepo.saveUpdateEvent(expectedAlbum.id, event.content, event.source) } returns event
            every { albumRepo.fetchById(expectedAlbum.id) } returns expectedAlbum
            val album = service.update(expectedAlbum.id, event.content, event.source)
            album shouldBe expectedAlbum
            verify { albumRepo.existsById(expectedAlbum.id) }
            verify { albumRepo.fetchDuplicateIds(expectedAlbum.id, duplicateIds) }
            verify { albumEventRepo.saveUpdateEvent(expectedAlbum.id, event.content, event.source) }
            verify { albumRepo.fetchById(expectedAlbum.id) }
            confirmVerified(albumRepo, photoRepo, albumEventRepo)
        }

        @Test
        fun `should return updated album (empty lists)`() {
            every { albumRepo.existsById(expectedAlbum.id) } returns true
            every {
                albumEventRepo.saveUpdateEvent(expectedAlbum.id, emptyEvent.content, emptyEvent.source)
            } returns emptyEvent
            every { albumRepo.fetchById(expectedAlbum.id) } returns expectedAlbum
            val album = service.update(expectedAlbum.id, emptyEvent.content, emptyEvent.source)
            album shouldBe expectedAlbum
            verify { albumRepo.existsById(expectedAlbum.id) }
            verify { albumEventRepo.saveUpdateEvent(expectedAlbum.id, emptyEvent.content, emptyEvent.source) }
            verify { albumRepo.fetchById(expectedAlbum.id) }
            confirmVerified(albumRepo, photoRepo, albumEventRepo)
        }
    }

    @Nested
    inner class updatePermissions {
        private val owner = User(
            id = UUID.randomUUID(),
            name = "owner",
            hashedPwd = "hashedPwd",
            role = Role.User,
            creationDate = OffsetDateTime.now()
        )
        private val user = User(
            id = UUID.randomUUID(),
            name = "user",
            hashedPwd = "hashedPwd",
            role = Role.User,
            creationDate = OffsetDateTime.now()
        )
        private val album = Album(
            id = UUID.randomUUID(),
            name = "test",
            ownerId = owner.id,
            creationDate = OffsetDateTime.now()
        )
        private val permissionsToAdd = setOf(
            UserPermissions(
                user = user,
                permissions = setOf(Permission.Read, Permission.Delete)
            )
        )
        private val permissionsToRemove = setOf(
            UserPermissions(
                user = user,
                permissions = setOf(Permission.Delete)
            )
        )
        private val event = AlbumEvent.UpdatePermissions(
            date = OffsetDateTime.now(),
            subjectId = album.id,
            number = 2,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            content = AlbumEvent.UpdatePermissions.Content(
                permissionsToAdd = setOf(
                    SubjectPermissions(
                        subjectId = user.id,
                        permissions = setOf(Permission.Read, Permission.Delete)
                    )
                ),
                permissionsToRemove = setOf(
                    SubjectPermissions(
                        subjectId = user.id,
                        permissions = setOf(Permission.Delete)
                    )
                )
            )
        )

        @Test
        fun `should throw exception if album does not exist`() {
            every { albumRepo.fetchById(album.id) } returns null
            val exception = assertThrows<EntityNotFoundException> {
                service.updatePermissions(album.id, permissionsToAdd, permissionsToRemove, event.source)
            }
            exception shouldHaveMessage "Album ${album.id} does not exist"
            verify { albumRepo.fetchById(album.id) }
            confirmVerified(albumRepo)
        }

        @Test
        fun `should throw exception if owner permission is deleted`() {
            every { albumRepo.fetchById(album.id) } returns album
            assertThrows<OwnerPermissionsUpdateException> {
                service.updatePermissions(
                    id = album.id,
                    permissionsToAdd = permissionsToAdd,
                    permissionsToRemove = setOf(
                        UserPermissions(
                            user = owner,
                            permissions = setOf(Permission.Delete)
                        )
                    ),
                    source = event.source
                )
            }
            verify { albumRepo.fetchById(album.id) }
            confirmVerified(albumRepo)
        }

        @Test
        fun `should update permissions of album`() {
            every { albumRepo.fetchById(album.id) } returns album
            every { albumEventRepo.saveUpdatePermissionsEvent(album.id, event.content, event.source) } returns event
            service.updatePermissions(album.id, permissionsToAdd, permissionsToRemove, event.source)
            verify { albumRepo.fetchById(album.id) }
            verify { albumEventRepo.saveUpdatePermissionsEvent(album.id, event.content, event.source) }
            confirmVerified(albumRepo, albumEventRepo)
        }
    }
}
