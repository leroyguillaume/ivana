@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
internal class AlbumEventRepositoryImplTest {
    private val pwdEncoder = BCryptPasswordEncoder()
    private val userCreationEventContent = UserEvent.Creation.Content(
        name = "admin",
        hashedPwd = pwdEncoder.encode("changeit"),
        role = Role.SuperAdmin
    )

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var userEventRepo: UserEventRepository

    @Autowired
    private lateinit var repo: AlbumEventRepositoryImpl

    @Autowired
    private lateinit var albumRepo: AlbumRepositoryImpl

    @Autowired
    private lateinit var authzRepo: UserAlbumAuthorizationRepositoryImpl

    @Autowired
    private lateinit var photoEventRepo: PhotoEventRepository

    @Autowired
    private lateinit var photoRepo: PhotoRepository

    private lateinit var userCreationEvent: UserEvent.Creation
    private lateinit var createdUser: User

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        userCreationEvent = userEventRepo.saveCreationEvent(userCreationEventContent, EventSource.System)
        createdUser = User(
            id = userCreationEvent.subjectId,
            name = userCreationEvent.content.name,
            hashedPwd = userCreationEvent.content.hashedPwd,
            role = userCreationEvent.content.role,
            creationDate = userCreationEvent.date
        )
    }

    @Nested
    inner class fetchDuplicateIds {
        private lateinit var source: EventSource.User
        private lateinit var photo1: Photo
        private lateinit var photo2: Photo
        private lateinit var albumCreationEvent: AlbumEvent.Creation

        @BeforeEach
        fun beforeEach() {
            source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1"))
            photo1 = photoEventRepo.saveUploadEvent(
                source = source,
                content = PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash1"
                )
            ).toPhoto(1)
            photo2 = photoEventRepo.saveUploadEvent(
                source = source,
                content = PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash2"
                )
            ).toPhoto(2)
            albumCreationEvent = repo.saveCreationEvent("album", source)
            repo.saveUpdateEvent(
                id = albumCreationEvent.subjectId,
                source = source,
                content = AlbumEvent.Update.Content(
                    name = albumCreationEvent.albumName,
                    photosToAdd = listOf(photo1.id, photo2.id),
                    photosToRemove = emptyList()
                )
            )
        }

        @Test
        fun `should return empty set if album does not exist`() {
            albumRepo.fetchDuplicateIds(UUID.randomUUID(), setOf(photo1.id, photo2.id)).shouldBeEmpty()
        }

        @Test
        fun `should return ids if album contains photos`() {
            albumRepo.fetchDuplicateIds(albumCreationEvent.subjectId, setOf(photo1.id, photo2.id)).shouldBe(
                setOf(photo1.id, photo2.id)
            )
        }
    }

    @Nested
    inner class fetch {
        private val subjectId = UUID.randomUUID()
        private val number = 1L

        @Test
        fun `should return null if event does not exist`() {
            val event = repo.fetch(number)
            event.shouldBeNull()
        }

        @Test
        fun `should return creation event with subject id and number`() {
            val expectedEvent = repo.saveCreationEvent(
                name = "album",
                source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1"))
            )
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return deletion event with subject id and number`() {
            val expectedEvent = repo.saveDeletionEvent(
                albumId = UUID.randomUUID(),
                source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1"))
            )
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return update event with subject id and number`() {
            val expectedEvent = repo.saveUpdateEvent(
                id = UUID.randomUUID(),
                source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1")),
                content = AlbumEvent.Update.Content(
                    name = "album",
                    photosToAdd = emptyList(),
                    photosToRemove = emptyList()
                )
            )
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }
    }

    @Nested
    inner class saveCreationEvent {
        private lateinit var expectedEvent: AlbumEvent.Creation
        private lateinit var expectedAlbum: Album

        @BeforeEach
        fun beforeEach() {
            expectedEvent = UUID.randomUUID().let { id ->
                AlbumEvent.Creation(
                    date = OffsetDateTime.now(),
                    subjectId = id,
                    number = 1,
                    source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1")),
                    albumName = "album"
                )
            }
            expectedAlbum = Album(
                id = expectedEvent.subjectId,
                ownerId = createdUser.id,
                name = expectedEvent.albumName,
                creationDate = expectedEvent.date
            )
        }

        @Test
        fun `should return created event`() {
            val event = repo.saveCreationEvent(expectedEvent.albumName, expectedEvent.source)
            event shouldBe expectedEvent.copy(
                date = event.date,
                subjectId = event.subjectId
            )
            albumRepo.fetchById(event.subjectId) shouldBe expectedAlbum.copy(
                id = event.subjectId,
                creationDate = event.date
            )
            val permissions = authzRepo.fetch(createdUser.id, event.subjectId)
            permissions shouldContainExactly Permission.values().toSet()
        }
    }

    @Nested
    inner class saveDeletionEvent {
        private lateinit var expectedEvent: AlbumEvent.Deletion

        @BeforeEach
        fun beforeEach() {
            val creationEvent = repo.saveCreationEvent(
                name = "album",
                source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1"))
            )
            expectedEvent = AlbumEvent.Deletion(
                date = OffsetDateTime.now(),
                subjectId = creationEvent.subjectId,
                number = 2,
                source = creationEvent.source
            )
        }

        @Test
        fun `should return created event`() {
            val event = repo.saveDeletionEvent(expectedEvent.subjectId, expectedEvent.source)
            event shouldBe expectedEvent.copy(
                date = event.date,
                subjectId = event.subjectId
            )
            albumRepo.fetchById(expectedEvent.subjectId).shouldBeNull()
        }
    }

    @Nested
    inner class saveUpdateEvent {
        private lateinit var source: EventSource.User
        private lateinit var photo1: Photo
        private lateinit var photo2: Photo
        private lateinit var albumCreationEvent: AlbumEvent.Creation
        private lateinit var expectedAddEvent: AlbumEvent.Update
        private lateinit var expectedRemoveEvent: AlbumEvent.Update
        private lateinit var expectedAlbum: Album

        @BeforeEach
        fun beforeEach() {
            source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1"))
            photo1 = photoEventRepo.saveUploadEvent(
                source = source,
                content = PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash1"
                )
            ).toPhoto(1)
            photo2 = photoEventRepo.saveUploadEvent(
                source = source,
                content = PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash2"
                )
            ).toPhoto(2)
            albumCreationEvent = repo.saveCreationEvent("album", source)
            expectedAddEvent = AlbumEvent.Update(
                date = OffsetDateTime.now(),
                subjectId = albumCreationEvent.subjectId,
                number = 2,
                source = source,
                content = AlbumEvent.Update.Content(
                    name = "album2",
                    photosToAdd = listOf(photo1.id, photo2.id),
                    photosToRemove = emptyList()
                )
            )
            expectedRemoveEvent = expectedAddEvent.copy(
                number = 3,
                content = expectedAddEvent.content.copy(
                    photosToAdd = emptyList(),
                    photosToRemove = expectedAddEvent.content.photosToAdd
                )
            )
            expectedAlbum = Album(
                id = expectedAddEvent.subjectId,
                ownerId = createdUser.id,
                name = expectedAddEvent.content.name,
                creationDate = albumCreationEvent.date
            )
        }

        @Test
        fun `should return created event (add)`() {
            val addEvent = repo.saveUpdateEvent(
                id = expectedAddEvent.subjectId,
                content = expectedAddEvent.content,
                source = expectedAddEvent.source
            )
            addEvent shouldBe expectedAddEvent.copy(
                date = addEvent.date,
                subjectId = addEvent.subjectId
            )
            albumRepo.fetchById(addEvent.subjectId) shouldBe expectedAlbum
            photoRepo.fetchAllOfAlbum(albumCreationEvent.subjectId, 0, 10).shouldContainExactly(listOf(photo1, photo2))
        }

        @Test
        fun `should return created event (remove)`() {
            repo.saveUpdateEvent(
                id = expectedAddEvent.subjectId,
                content = expectedAddEvent.content,
                source = expectedAddEvent.source
            )
            val removeEvent = repo.saveUpdateEvent(
                id = expectedRemoveEvent.subjectId,
                content = expectedRemoveEvent.content,
                source = expectedRemoveEvent.source
            )
            removeEvent shouldBe expectedRemoveEvent.copy(
                date = removeEvent.date,
                subjectId = removeEvent.subjectId
            )
            albumRepo.fetchById(removeEvent.subjectId) shouldBe expectedAlbum
            photoRepo.fetchAllOfAlbum(albumCreationEvent.subjectId, 0, 10).shouldBeEmpty()
        }
    }
}
