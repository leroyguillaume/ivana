@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
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
    private lateinit var albumEventRepo: AlbumEventRepository
    private lateinit var photoRepo: PhotoRepository
    private lateinit var service: AlbumServiceImpl

    @BeforeEach
    fun beforeEach() {
        albumRepo = mockk()
        albumEventRepo = mockk()
        photoRepo = mockk()

        service = AlbumServiceImpl(albumRepo, albumEventRepo, photoRepo)
    }

    @Nested
    inner class create {
        private val creationEvent = AlbumEvent.Creation(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            albumName = "album"
        )
        private val expectedAlbum = Album(
            id = creationEvent.subjectId,
            ownerId = creationEvent.source.id,
            name = creationEvent.albumName,
            creationDate = creationEvent.date
        )

        @Test
        fun `should save creation event`() {
            every {
                albumEventRepo.saveCreationEvent(creationEvent.albumName, creationEvent.source)
            } returns creationEvent
            every { albumRepo.fetchById(expectedAlbum.id) } returns expectedAlbum
            val album = service.create(creationEvent.albumName, creationEvent.source)
            album shouldBe expectedAlbum
            verify { albumEventRepo.saveCreationEvent(creationEvent.albumName, creationEvent.source) }
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
    inner class getAllPhotos {
        private val albumId = UUID.randomUUID()
        private val pageNo = 1
        private val pageSize = 3
        private val expectedPage = Page(
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
        fun `should return page`() {
            every { photoRepo.fetchAllOfAlbum(albumId, pageNo - 1, pageSize) } returns expectedPage.content
            every { photoRepo.countOfAlbum(albumId) } returns expectedPage.totalItems
            val page = service.getAllPhotos(albumId, pageNo, pageSize)
            page shouldBe expectedPage
            verify { photoRepo.fetchAllOfAlbum(albumId, pageNo - 1, pageSize) }
            verify { photoRepo.countOfAlbum(albumId) }
            confirmVerified(photoRepo)
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
        fun `should throw exception if photos does not exist`() {
            every { albumRepo.existsById(expectedAlbum.id) } returns true
            every { photoRepo.fetchExistingIds(duplicateIds) } returns emptySet()
            val exception = assertThrows<PhotosNotFoundException> {
                service.update(expectedAlbum.id, event.content, event.source)
            }
            exception.photosIds shouldBe duplicateIds
            verify { albumRepo.existsById(expectedAlbum.id) }
            verify { photoRepo.fetchExistingIds(duplicateIds) }
            confirmVerified(albumRepo, photoRepo)
        }

        @Test
        fun `should throw exception if album already contains photos`() {
            every { albumRepo.existsById(expectedAlbum.id) } returns true
            every { photoRepo.fetchExistingIds(duplicateIds) } returns duplicateIds
            every { albumRepo.fetchDuplicateIds(expectedAlbum.id, duplicateIds) } returns duplicateIds
            val exception = assertThrows<AlbumAlreadyContainsPhotosException> {
                service.update(expectedAlbum.id, event.content, event.source)
            }
            exception.photosIds shouldBe duplicateIds
            verify { albumRepo.existsById(expectedAlbum.id) }
            verify { photoRepo.fetchExistingIds(duplicateIds) }
            verify { albumRepo.fetchDuplicateIds(expectedAlbum.id, duplicateIds) }
            confirmVerified(albumRepo, photoRepo)
        }

        @Test
        fun `should return updated album`() {
            every { albumRepo.existsById(expectedAlbum.id) } returns true
            every { photoRepo.fetchExistingIds(duplicateIds) } returns duplicateIds
            every { albumRepo.fetchDuplicateIds(expectedAlbum.id, duplicateIds) } returns emptySet()
            every { albumEventRepo.saveUpdateEvent(expectedAlbum.id, event.content, event.source) } returns event
            every { albumRepo.fetchById(expectedAlbum.id) } returns expectedAlbum
            val album = service.update(expectedAlbum.id, event.content, event.source)
            album shouldBe expectedAlbum
            verify { albumRepo.existsById(expectedAlbum.id) }
            verify { photoRepo.fetchExistingIds(duplicateIds) }
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
}
