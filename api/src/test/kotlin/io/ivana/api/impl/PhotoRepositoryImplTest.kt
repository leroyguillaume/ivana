@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
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
import java.util.*

@SpringBootTest
internal class PhotoRepositoryImplTest {
    private val pwdEncoder = BCryptPasswordEncoder()
    private val initData = listOf(
        InitDataEntry(
            userCreationContent = UserEvent.Creation.Content(
                name = "admin",
                hashedPwd = pwdEncoder.encode("changeit"),
                role = Role.SuperAdmin
            ),
            photoUploadContents = listOf(
                PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash1"
                ),
                PhotoEvent.Upload.Content(
                    type = Photo.Type.Png,
                    hash = "hash2"
                ),
                PhotoEvent.Upload.Content(
                    type = Photo.Type.Png,
                    hash = "hash3"
                )
            )
        ),
        InitDataEntry(
            userCreationContent = UserEvent.Creation.Content(
                name = "gleroy",
                hashedPwd = pwdEncoder.encode("changeit"),
                role = Role.User
            ),
            photoUploadContents = listOf(
                PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash4"
                ),
                PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash5"
                )
            )
        )
    )

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var repo: PhotoRepositoryImpl

    @Autowired
    private lateinit var eventRepo: PhotoEventRepository

    @Autowired
    private lateinit var userEventRepo: UserEventRepository

    @Autowired
    private lateinit var albumEventRepo: AlbumEventRepository

    private lateinit var uploadedPhotos: List<Photo>

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        var no = 1
        uploadedPhotos = initData
            .map { entry ->
                val user = userEventRepo.saveCreationEvent(entry.userCreationContent, EventSource.System).toUser()
                entry.photoUploadContents.map { content ->
                    eventRepo.saveUploadEvent(
                        content = content,
                        source = EventSource.User(user.id, InetAddress.getByName("127.0.0.1"))
                    ).toPhoto(no++)
                }
            }
            .flatten()
        val photo = uploadedPhotos[uploadedPhotos.size - 1]
        eventRepo.saveUpdatePermissionsEvent(
            photoId = photo.id,
            content = PhotoEvent.UpdatePermissions.Content(
                permissionsToAdd = emptySet(),
                permissionsToRemove = setOf(
                    SubjectPermissions(
                        subjectId = uploadedPhotos[0].ownerId,
                        permissions = setOf(Permission.Read)
                    )
                )
            ),
            source = EventSource.User(photo.ownerId, InetAddress.getByName("127.0.0.1"))
        )
    }

    @Nested
    inner class count {
        @Test
        fun `should return count of photos of user`() {
            val count = repo.count()
            count shouldBe uploadedPhotos.size
        }
    }

    @Nested
    inner class `count with owner id` {
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            ownerId = uploadedPhotos[0].ownerId
        }

        @Test
        fun `should return count of photos of user`() {
            val count = repo.count(ownerId)
            count shouldBe 3
        }
    }

    @Nested
    inner class countOfAlbum {
        private lateinit var ownerId: UUID
        private lateinit var albumCreationEvent: AlbumEvent.Creation

        @BeforeEach
        fun beforeEach() {
            ownerId = uploadedPhotos[0].ownerId
            val source = EventSource.User(ownerId, InetAddress.getByName("127.0.0.1"))
            albumCreationEvent = albumEventRepo.saveCreationEvent("album", source)
            albumEventRepo.saveUpdateEvent(
                id = albumCreationEvent.subjectId,
                content = AlbumEvent.Update.Content(
                    name = albumCreationEvent.albumName,
                    photosToAdd = uploadedPhotos.map { it.id },
                    photosToRemove = emptyList()
                ),
                source = source
            )
        }

        @Test
        fun `should return count of photos of album`() {
            val count = repo.countOfAlbum(albumCreationEvent.subjectId, ownerId)
            count shouldBe uploadedPhotos.size - 1
        }
    }

    @Nested
    inner class existsById {
        private lateinit var photo: Photo

        @BeforeEach
        fun beforeEach() {
            photo = uploadedPhotos[0]
        }

        @Test
        fun `should return false if photo does not exist`() {
            val exists = repo.existsById(UUID.randomUUID())
            exists.shouldBeFalse()
        }

        @Test
        fun `should return true if photo exists`() {
            val exists = repo.existsById(photo.id)
            exists.shouldBeTrue()
        }
    }

    @Nested
    inner class fetchAll {
        @Test
        fun `should return all photos in interval`() {
            val photos = repo.fetchAll(1, 10)
            photos shouldBe uploadedPhotos.sortedBy { it.id.toString() }.subList(1, uploadedPhotos.size)
        }
    }

    @Nested
    inner class `fetchAll with owner id` {
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            ownerId = uploadedPhotos[0].ownerId
        }

        @Test
        fun `should return all photos in interval`() {
            val photos = repo.fetchAll(ownerId, 1, 10)
            photos shouldBe uploadedPhotos.subList(1, 3)
        }
    }

    @Nested
    inner class fetchAllByIds {
        @Test
        fun `should return empty set if ids is empty`() {
            val photos = repo.fetchAllByIds(emptySet())
            photos.shouldBeEmpty()
        }

        @Test
        fun `should return all photos`() {
            val photos = repo.fetchAllByIds(uploadedPhotos.map { it.id }.toSet())
            photos shouldContainExactlyInAnyOrder uploadedPhotos
        }
    }

    @Nested
    inner class fetchAllOfAlbum {
        private lateinit var ownerId: UUID
        private lateinit var albumCreationEvent: AlbumEvent.Creation

        @BeforeEach
        fun beforeEach() {
            ownerId = uploadedPhotos[0].ownerId
            val source = EventSource.User(ownerId, InetAddress.getByName("127.0.0.1"))
            albumCreationEvent = albumEventRepo.saveCreationEvent("album", source)
            albumEventRepo.saveUpdateEvent(
                id = albumCreationEvent.subjectId,
                content = AlbumEvent.Update.Content(
                    name = albumCreationEvent.albumName,
                    photosToAdd = uploadedPhotos.map { it.id },
                    photosToRemove = emptyList()
                ),
                source = source
            )
        }

        @Test
        fun `should return all photos in interval`() {
            val photos = repo.fetchAllOfAlbum(albumCreationEvent.subjectId, ownerId, 1, 10)
            photos shouldBe uploadedPhotos.subList(1, 4)
        }
    }

    @Nested
    inner class fetchById {
        private lateinit var photo: Photo

        @BeforeEach
        fun beforeEach() {
            photo = uploadedPhotos[0]
        }

        @Test
        fun `should return null if photo does not exist`() {
            val photo = repo.fetchById(UUID.randomUUID())
            photo.shouldBeNull()
        }

        @Test
        fun `should return photo with id`() {
            val photo = repo.fetchById(photo.id)
            photo shouldBe photo
        }
    }

    @Nested
    inner class fetchByHash {
        private lateinit var photo: Photo

        @BeforeEach
        fun beforeEach() {
            photo = uploadedPhotos[0]
        }

        @Test
        fun `should return null if photo does not exist`() {
            val photo = repo.fetchByHash(photo.ownerId, photo.hash.reversed())
            photo.shouldBeNull()
        }

        @Test
        fun `should return null if owner is not the same`() {
            val photo = repo.fetchByHash(UUID.randomUUID(), photo.hash)
            photo.shouldBeNull()
        }

        @Test
        fun `should return photo with hash`() {
            val photo = repo.fetchByHash(photo.ownerId, photo.hash)
            photo shouldBe photo
        }
    }

    @Nested
    inner class fetchExistingIds {
        private lateinit var expectedExistingIds: Set<UUID>

        @BeforeEach
        fun beforeEach() {
            expectedExistingIds = uploadedPhotos.map { it.id }.toSet()
        }

        @Test
        fun `should return existing ids`() {
            val existingIds = repo.fetchExistingIds(expectedExistingIds + setOf(UUID.randomUUID()))
            existingIds shouldBe expectedExistingIds
        }
    }

    @Nested
    inner class fetchNextOf {
        @Test
        fun `should return null if photo does not exist`() {
            val photo = repo.fetchNextOf(uploadedPhotos[4])
            photo.shouldBeNull()
        }

        @Test
        fun `should return null if next photo does not have same owner`() {
            val photo = repo.fetchNextOf(uploadedPhotos[2])
            photo.shouldBeNull()
        }

        @Test
        fun `should return closet photo after`() {
            val photo = repo.fetchNextOf(uploadedPhotos[1])
            photo shouldBe uploadedPhotos[2]
        }
    }

    @Nested
    inner class fetchPreviousOf {
        @Test
        fun `should return null if photo does not exist`() {
            val photo = repo.fetchPreviousOf(uploadedPhotos[0])
            photo.shouldBeNull()
        }

        @Test
        fun `should return null if previous photo does not have same owner`() {
            val photo = repo.fetchPreviousOf(uploadedPhotos[3])
            photo.shouldBeNull()
        }

        @Test
        fun `should return closet photo after`() {
            val photo = repo.fetchPreviousOf(uploadedPhotos[2])
            photo shouldBe uploadedPhotos[1]
        }
    }

    private data class InitDataEntry(
        val userCreationContent: UserEvent.Creation.Content,
        val photoUploadContents: List<PhotoEvent.Upload.Content>
    )
}
