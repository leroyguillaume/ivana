@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.Album
import io.ivana.core.Permission
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
internal class AlbumRepositoryImplTest : AbstractRepositoryTest() {
    @Nested
    inner class count {
        @Test
        fun `should return count of albums`() {
            val count = albumRepo.count()
            count shouldBe albumCreationEvents.size
        }
    }

    @Nested
    inner class `count with owner id` {
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            ownerId = albumCreationEvents[0].source.id
        }

        @Test
        fun `should return count of albums of user`() {
            val count = albumRepo.count(ownerId)
            count shouldBe 3
        }
    }

    @Nested
    inner class countShared {
        private lateinit var userId: UUID

        @BeforeEach
        fun beforeEach() {
            userId = userCreationEvents[1].subjectId
        }

        @Test
        fun `should return count of shared albums of user`() {
            val count = albumRepo.countShared(userId)
            count shouldBe 1
        }
    }

    @Nested
    inner class existsById {
        private lateinit var albumId: UUID

        @BeforeEach
        fun beforeEach() {
            albumId = albumCreationEvents[0].subjectId
        }

        @Test
        fun `should return false if album does not exist`() {
            val exists = albumRepo.existsById(UUID.randomUUID())
            exists.shouldBeFalse()
        }

        @Test
        fun `should return true if album exists`() {
            val exists = albumRepo.existsById(albumId)
            exists.shouldBeTrue()
        }
    }

    @Nested
    inner class fetchAll {
        private lateinit var createdAlbums: List<Album>

        @BeforeEach
        fun beforeEach() {
            createdAlbums = albumCreationEvents
                .map { it.toAlbum() }
                .sortedBy { it.id.toString() }
        }

        @Test
        fun `should return all albums in interval`() {
            val albums = albumRepo.fetchAll(1, 10)
            albums shouldBe createdAlbums.subList(1, createdAlbums.size)
        }
    }

    @Nested
    inner class `fetchAll with owner id` {
        private lateinit var createdAlbums: List<Album>
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            createdAlbums = albumCreationEvents
                .filter { it.source.id == userCreationEvents[0].subjectId }
                .map { it.toAlbum() }
                .sortedBy { it.name }
            ownerId = createdAlbums[0].ownerId
        }

        @Test
        fun `should return all albums in interval`() {
            val photos = albumRepo.fetchAll(ownerId, 1, 10)
            photos shouldBe createdAlbums.subList(1, 3)
        }
    }

    @Nested
    inner class fetchAllByIds {
        private lateinit var createdAlbums: List<Album>

        @BeforeEach
        fun beforeEach() {
            createdAlbums = albumCreationEvents.map { it.toAlbum() }
        }

        @Test
        fun `should return empty set if ids is empty`() {
            val albums = albumRepo.fetchAllByIds(emptySet())
            albums.shouldBeEmpty()
        }

        @Test
        fun `should return all albums`() {
            val albums = albumRepo.fetchAllByIds(createdAlbums.map { it.id }.toSet())
            albums shouldContainExactlyInAnyOrder createdAlbums
        }
    }

    @Nested
    inner class fetchById {
        private lateinit var createdAlbum: Album

        @BeforeEach
        fun beforeEach() {
            createdAlbum = albumCreationEvents[0].toAlbum()
        }

        @Test
        fun `should return null if album does not exist`() {
            val album = albumRepo.fetchById(UUID.randomUUID())
            album.shouldBeNull()
        }

        @Test
        fun `should return album with id`() {
            val album = albumRepo.fetchById(createdAlbum.id)
            album shouldBe createdAlbum
        }
    }

    @Nested
    inner class fetchDuplicateIds {
        private lateinit var albumId: UUID
        private lateinit var expectedDuplicateIds: Set<UUID>

        @BeforeEach
        fun beforeEach() {
            val albumUpdateEvent = albumUpdateEvents[0]
            albumId = albumUpdateEvent.subjectId
            expectedDuplicateIds = albumUpdateEvent.content.photosToAdd.toSet()
        }

        @Test
        fun `should return empty set if album does not exist`() {
            val duplicateIds = albumRepo.fetchDuplicateIds(UUID.randomUUID(), expectedDuplicateIds)
            duplicateIds.shouldBeEmpty()
        }

        @Test
        fun `should return ids if album contains photos`() {
            val duplicateIds = albumRepo.fetchDuplicateIds(albumId, expectedDuplicateIds)
            duplicateIds shouldBe expectedDuplicateIds
        }
    }

    @Nested
    inner class fetchExistingIds {
        private lateinit var expectedExistingIds: Set<UUID>

        @BeforeEach
        fun beforeEach() {
            expectedExistingIds = albumCreationEvents.map { it.subjectId }.toSet()
        }

        @Test
        fun `should return existing ids`() {
            val existingIds = albumRepo.fetchExistingIds(expectedExistingIds + setOf(UUID.randomUUID()))
            existingIds shouldBe expectedExistingIds
        }
    }

    @Nested
    inner class fetchOrder {
        private lateinit var albumId: UUID

        @BeforeEach
        fun beforeEach() {
            albumId = albumCreationEvents[0].subjectId
        }

        @Test
        fun `should return null if album does not exist`() {
            val order = albumRepo.fetchOrder(UUID.randomUUID(), UUID.randomUUID())
            order.shouldBeNull()
        }

        @Test
        fun `should return null if photo is not in album`() {
            val order = albumRepo.fetchOrder(albumId, photoUploadEvents[3].subjectId)
            order.shouldBeNull()
        }

        @Test
        fun `should return order of photo in album`() {
            val order = albumRepo.fetchOrder(albumId, photoUploadEvents[1].subjectId)
            order shouldBe 2
        }
    }

    @Nested
    inner class fetchShared {
        private lateinit var createdAlbums: List<Album>
        private lateinit var userId: UUID

        @BeforeEach
        fun beforeEach() {
            userId = userCreationEvents[0].subjectId
            createdAlbums = listOf(
                albumCreationEvents[6].toAlbum(),
                albumCreationEvents[7].toAlbum(),
                albumCreationEvents[8].toAlbum()
            )
        }

        @Test
        fun `should return all albums in interval`() {
            val photos = albumRepo.fetchShared(userId, 1, 10)
            photos shouldBe createdAlbums.subList(1, 3)
        }
    }

    @Nested
    inner class fetchSize {
        private lateinit var albumId: UUID
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            val albumCreationEvent = albumCreationEvents[0]
            albumId = albumCreationEvent.subjectId
            ownerId = albumCreationEvent.source.id
        }

        @Test
        fun `should return count of photos of album (owner)`() {
            val count = albumRepo.fetchSize(albumId, ownerId)
            count shouldBe 3
        }

        @Test
        fun `should return count of photos of album (other user)`() {
            val count = albumRepo.fetchSize(albumId, userCreationEvents[1].subjectId)
            count shouldBe 2
        }
    }

    @Nested
    inner class suggest {
        private lateinit var userId: UUID

        @BeforeEach
        fun beforeEach() {
            userId = userCreationEvents[1].subjectId
        }

        @Test
        fun `should return empty list`() {
            albumRepo.suggest("album2", 10, userId, Permission.Read).shouldBeEmpty()
        }

        @Test
        fun `should return album1`() {
            albumRepo.suggest("album1", 10, userId, Permission.Read) shouldContainExactly listOf(
                albumCreationEvents[0].toAlbum()
            )
        }

        @Test
        fun `should return album2`() {
            albumRepo.suggest("album2", 10, userId, Permission.Update) shouldContainExactly listOf(
                albumCreationEvents[1].toAlbum()
            )
        }

        @Test
        fun `should return album3`() {
            albumRepo.suggest("album3", 10, userId, Permission.Delete) shouldContainExactly listOf(
                albumCreationEvents[2].toAlbum()
            )
        }

        @Test
        fun `should return album7`() {
            albumRepo.suggest("album7", 10, userId, Permission.UpdatePermissions) shouldContainExactly listOf(
                albumCreationEvents[6].toAlbum()
            )
        }

        @Test
        fun `should return album1 and album4`() {
            albumRepo.suggest("album", 2, userId, Permission.Read) shouldContainExactly listOf(
                albumCreationEvents[0].toAlbum(),
                albumCreationEvents[3].toAlbum()
            )
        }
    }
}
