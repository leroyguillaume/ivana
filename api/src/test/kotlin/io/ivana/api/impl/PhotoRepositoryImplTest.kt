@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.Photo
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
internal class PhotoRepositoryImplTest : AbstractRepositoryTest() {
    @Nested
    inner class count {
        @Test
        fun `should return count of photos of user`() {
            val count = photoRepo.count()
            count shouldBe photoUploadEvents.size
        }
    }

    @Nested
    inner class countShared {
        @Test
        fun `should return count of shared photos of user`() {
            val count = photoRepo.countShared(userCreationEvents[1].subjectId)
            count shouldBe 2
        }
    }

    @Nested
    inner class `count with owner id` {
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            ownerId = photoUploadEvents[0].source.id
        }

        @Test
        fun `should return count of photos of user`() {
            val count = photoRepo.count(ownerId)
            count shouldBe 3
        }
    }

    @Nested
    inner class existsById {
        private lateinit var photoId: UUID

        @BeforeEach
        fun beforeEach() {
            photoId = photoUploadEvents[0].subjectId
        }

        @Test
        fun `should return false if photo does not exist`() {
            val exists = photoRepo.existsById(UUID.randomUUID())
            exists.shouldBeFalse()
        }

        @Test
        fun `should return true if photo exists`() {
            val exists = photoRepo.existsById(photoId)
            exists.shouldBeTrue()
        }
    }

    @Nested
    inner class fetchAll {
        private lateinit var uploadedPhotos: List<Photo>

        @BeforeEach
        fun beforeEach() {
            uploadedPhotos = photoUploadEvents
                .map { it.toPhoto() }
                .sortedBy { it.id.toString() }
        }

        @Test
        fun `should return all photos in interval`() {
            val photos = photoRepo.fetchAll(1, 10)
            photos shouldBe uploadedPhotos.subList(1, uploadedPhotos.size)
        }
    }

    @Nested
    inner class `fetchAll with owner id` {
        private lateinit var uploadedPhotos: List<Photo>
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            ownerId = userCreationEvents[0].subjectId
            uploadedPhotos = photoUploadEvents
                .filter { it.source.id == ownerId }
                .map { it.toPhoto() }
                .sortedBy { it.no }
        }

        @Test
        fun `should return all photos in interval`() {
            val photos = photoRepo.fetchAll(ownerId, 1, 10)
            photos shouldBe uploadedPhotos.subList(1, 3)
        }
    }

    @Nested
    inner class fetchAllByIds {
        private lateinit var uploadedPhotos: List<Photo>

        @BeforeEach
        fun beforeEach() {
            uploadedPhotos = photoUploadEvents.map { it.toPhoto() }
        }

        @Test
        fun `should return empty set if ids is empty`() {
            val photos = photoRepo.fetchAllByIds(emptySet())
            photos.shouldBeEmpty()
        }

        @Test
        fun `should return all photos`() {
            val photos = photoRepo.fetchAllByIds(uploadedPhotos.map { it.id }.toSet())
            photos shouldContainExactlyInAnyOrder uploadedPhotos
        }
    }

    @Nested
    inner class fetchAllOfAlbum {
        private lateinit var albumId: UUID
        private lateinit var ownerId: UUID
        private lateinit var expectedPhotos: List<Photo>

        @BeforeEach
        fun beforeEach() {
            val albumCreationEvent = albumCreationEvents[0]
            albumId = albumCreationEvent.subjectId
            ownerId = albumCreationEvent.source.id
            expectedPhotos = photoUploadEvents
                .filter { it.source.id == ownerId }
                .map { it.toPhoto() }
                .sortedBy { it.no }
        }

        @Test
        fun `should return all photos in interval (owner)`() {
            val photos = photoRepo.fetchAllOfAlbum(albumId, ownerId, 1, 10)
            photos shouldBe expectedPhotos.subList(1, expectedPhotos.size)
        }

        @Test
        fun `should return all photos in interval (other user)`() {
            val photos = photoRepo.fetchAllOfAlbum(albumId, userCreationEvents[1].subjectId, 1, 10)
            photos shouldBe expectedPhotos
                .filter { it.id != photoUploadEvents[1].subjectId }
                .subList(1, expectedPhotos.size - 1)
        }
    }

    @Nested
    inner class fetchById {
        private lateinit var expectedPhoto: Photo

        @BeforeEach
        fun beforeEach() {
            expectedPhoto = photoUploadEvents[0].toPhoto()
        }

        @Test
        fun `should return null if photo does not exist`() {
            val photo = photoRepo.fetchById(UUID.randomUUID())
            photo.shouldBeNull()
        }

        @Test
        fun `should return photo with id`() {
            val photo = photoRepo.fetchById(expectedPhoto.id)
            photo shouldBe expectedPhoto
        }
    }

    @Nested
    inner class fetchByHash {
        private lateinit var expectedPhoto: Photo

        @BeforeEach
        fun beforeEach() {
            expectedPhoto = photoUploadEvents[0].toPhoto()
        }

        @Test
        fun `should return null if photo does not exist`() {
            val photo = photoRepo.fetchByHash(expectedPhoto.ownerId, expectedPhoto.hash.reversed())
            photo.shouldBeNull()
        }

        @Test
        fun `should return null if owner is not the same`() {
            val photo = photoRepo.fetchByHash(UUID.randomUUID(), expectedPhoto.hash)
            photo.shouldBeNull()
        }

        @Test
        fun `should return photo with hash`() {
            val photo = photoRepo.fetchByHash(expectedPhoto.ownerId, expectedPhoto.hash)
            photo shouldBe expectedPhoto
        }
    }

    @Nested
    inner class fetchExistingIds {
        private lateinit var expectedExistingIds: Set<UUID>

        @BeforeEach
        fun beforeEach() {
            expectedExistingIds = photoUploadEvents.map { it.subjectId }.toSet()
        }

        @Test
        fun `should return existing ids`() {
            val existingIds = photoRepo.fetchExistingIds(expectedExistingIds + setOf(UUID.randomUUID()))
            existingIds shouldBe expectedExistingIds
        }
    }

    @Nested
    inner class fetchNext {
        @Test
        fun `should be null if no is last`() {
            val photo = photoRepo.fetchNextOf(3)
            photo.shouldBeNull()
        }

        @Test
        fun `should be next photo (same owner)`() {
            val photo = photoRepo.fetchNextOf(1)
            photo shouldBe photoUploadEvents[1].toPhoto()
        }
    }

    @Nested
    inner class `fetchNext with user id` {
        @Test
        fun `should be null if no is last`() {
            val photo = photoRepo.fetchNextOf(8, userCreationEvents[0].subjectId)
            photo.shouldBeNull()
        }

        @Test
        fun `should be next photo (same owner)`() {
            val photo = photoRepo.fetchNextOf(2, userCreationEvents[0].subjectId)
            photo shouldBe photoUploadEvents[2].toPhoto()
        }

        @Test
        fun `should be next photo (other owner)`() {
            val photo = photoRepo.fetchNextOf(3, userCreationEvents[0].subjectId)
            photo shouldBe photoUploadEvents[7].toPhoto()
        }

        @Test
        fun `should be next photo (in album)`() {
            val photo = photoRepo.fetchNextOf(1, userCreationEvents[1].subjectId)
            photo shouldBe photoUploadEvents[2].toPhoto()
        }
    }

    @Nested
    inner class `fetchNext with album id` {
        private lateinit var albumId: UUID
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            val albumCreationEvent = albumCreationEvents[0]
            ownerId = albumCreationEvent.source.id
            albumId = albumCreationEvent.subjectId
        }

        @Test
        fun `should be null if no is last`() {
            val photo = photoRepo.fetchNextOf(3, ownerId, albumId)
            photo.shouldBeNull()
        }

        @Test
        fun `should be next photo (same owner)`() {
            val photo = photoRepo.fetchNextOf(1, ownerId, albumId)
            photo shouldBe photoUploadEvents[1].toPhoto()
        }

        @Test
        fun `should be next photo (other owner)`() {
            val photo = photoRepo.fetchNextOf(1, userCreationEvents[1].subjectId, albumId)
            photo shouldBe photoUploadEvents[2].toPhoto()
        }
    }

    @Nested
    inner class fetchPrevious {
        @Test
        fun `should be null if no is first`() {
            val photo = photoRepo.fetchPreviousOf(1)
            photo.shouldBeNull()
        }

        @Test
        fun `should be previous photo (same owner)`() {
            val photo = photoRepo.fetchPreviousOf(2)
            photo shouldBe photoUploadEvents[0].toPhoto()
        }
    }

    @Nested
    inner class `fetchPrevious with user id` {
        @Test
        fun `should be null if no is first`() {
            val photo = photoRepo.fetchPreviousOf(0, userCreationEvents[0].subjectId)
            photo.shouldBeNull()
        }

        @Test
        fun `should be previous photo (same owner)`() {
            val photo = photoRepo.fetchPreviousOf(2, userCreationEvents[0].subjectId)
            photo shouldBe photoUploadEvents[0].toPhoto()
        }

        @Test
        fun `should be previous photo (other owner)`() {
            val photo = photoRepo.fetchPreviousOf(8, userCreationEvents[0].subjectId)
            photo shouldBe photoUploadEvents[2].toPhoto()
        }

        @Test
        fun `should be previous photo (in album)`() {
            val photo = photoRepo.fetchPreviousOf(3, userCreationEvents[1].subjectId)
            photo shouldBe photoUploadEvents[0].toPhoto()
        }
    }

    @Nested
    inner class `fetchPrevious with album id` {
        private lateinit var albumId: UUID
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            val albumCreationEvent = albumCreationEvents[0]
            ownerId = albumCreationEvent.source.id
            albumId = albumCreationEvent.subjectId
        }

        @Test
        fun `should be null if no is first`() {
            val photo = photoRepo.fetchPreviousOf(1, ownerId, albumId)
            photo.shouldBeNull()
        }

        @Test
        fun `should be previous photo (same owner)`() {
            val photo = photoRepo.fetchPreviousOf(2, ownerId, albumId)
            photo shouldBe photoUploadEvents[0].toPhoto()
        }

        @Test
        fun `should be next photo (other owner)`() {
            val photo = photoRepo.fetchPreviousOf(3, userCreationEvents[1].subjectId, albumId)
            photo shouldBe photoUploadEvents[0].toPhoto()
        }
    }

    @Nested
    inner class fetchShared {
        private lateinit var uploadedPhotos: List<Photo>
        private lateinit var userId: UUID

        @BeforeEach
        fun beforeEach() {
            userId = userCreationEvents[1].subjectId
            uploadedPhotos = listOf(photoUploadEvents[0], photoUploadEvents[2]).map { it.toPhoto() }
        }

        @Test
        fun `should return all photos in interval`() {
            val photos = photoRepo.fetchShared(userId, 1, 10)
            photos shouldBe uploadedPhotos.subList(1, 2)
        }
    }
}
