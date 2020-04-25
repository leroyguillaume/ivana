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
                    no = 1
                ),
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = UUID.randomUUID(),
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash2",
                    no = 2
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
}
