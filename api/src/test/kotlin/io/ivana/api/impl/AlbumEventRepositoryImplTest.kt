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
import org.springframework.boot.test.context.SpringBootTest
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
internal class AlbumEventRepositoryImplTest : AbstractRepositoryTest() {
    @Nested
    inner class fetch {
        private lateinit var albumId: UUID
        private lateinit var source: EventSource.User

        @BeforeEach
        fun beforeEach() {
            val albumCreationEvent = albumCreationEvents[0]
            albumId = albumCreationEvent.subjectId
            source = userLocalSource(albumCreationEvent.source.id)
        }

        @Test
        fun `should return null if event does not exist`() {
            val event = albumEventRepo.fetch(nextAlbumEventNumber())
            event.shouldBeNull()
        }

        @Test
        fun `should return creation event with number`() {
            val expectedEvent = albumCreationEvents[0]
            val event = albumEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return deletion event with number`() {
            val expectedEvent = albumEventRepo.saveDeletionEvent(albumId, source)
            val event = albumEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return update event with number`() {
            val expectedEvent = albumUpdateEvents[0]
            val event = albumEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return update permissions event with number`() {
            val expectedEvent = albumUpdatePermissionsEvents[0]
            val event = albumEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }
    }

    @Nested
    inner class saveCreationEvent {
        private lateinit var event: AlbumEvent.Creation

        @BeforeEach
        fun beforeEach() {
            event = albumCreationEvents[0]
        }

        @Test
        fun `should save album`() {
            albumRepo.fetchById(event.subjectId) shouldBe event.toAlbum()
            val permissions = userAlbumAuthzRepo.fetch(event.source.id, event.subjectId)
            permissions shouldContainExactly Permission.values().toSet()
        }
    }

    @Nested
    inner class saveDeletionEvent {
        private lateinit var expectedEvent: AlbumEvent.Deletion

        @BeforeEach
        fun beforeEach() {
            val albumCreationEvent = albumCreationEvents[0]
            expectedEvent = AlbumEvent.Deletion(
                date = OffsetDateTime.now(),
                subjectId = albumCreationEvent.subjectId,
                number = nextAlbumEventNumber(),
                source = albumCreationEvent.source
            )
        }

        @Test
        fun `should return created event`() {
            val event = albumEventRepo.saveDeletionEvent(expectedEvent.subjectId, expectedEvent.source)
            event shouldBe expectedEvent.copy(date = event.date)
            albumRepo.fetchById(expectedEvent.subjectId).shouldBeNull()
        }
    }

    @Nested
    inner class saveUpdateEvent {
        private lateinit var expectedEvent: AlbumEvent.Update
        private lateinit var expectedAlbum: Album

        @BeforeEach
        fun beforeEach() {
            val albumCreationEvent = albumCreationEvents[0]
            expectedEvent = AlbumEvent.Update(
                date = OffsetDateTime.now(),
                subjectId = albumCreationEvent.subjectId,
                number = nextAlbumEventNumber(),
                source = albumCreationEvent.source,
                content = AlbumEvent.Update.Content(
                    name = "new${albumCreationEvent.content.name}",
                    photosToAdd = listOf(photoUploadEvents[3].subjectId),
                    photosToRemove = listOf(photoUploadEvents[0].subjectId)
                )
            )
            expectedAlbum = albumCreationEvent.copy(
                content = AlbumEvent.Creation.Content(name = expectedEvent.content.name)
            ).toAlbum()
        }

        @Test
        fun `should return created event`() {
            val event = albumEventRepo.saveUpdateEvent(
                albumId = expectedEvent.subjectId,
                content = expectedEvent.content,
                source = expectedEvent.source
            )
            event shouldBe expectedEvent.copy(date = event.date)
            albumRepo.fetchById(expectedEvent.subjectId) shouldBe expectedAlbum
            val photosIds = photoRepo.fetchAllOfAlbum(expectedEvent.subjectId, expectedEvent.source.id, 0, 10)
                .map { it.id }
            photosIds shouldContainExactly listOf(
                photoUploadEvents[1].subjectId,
                photoUploadEvents[2].subjectId,
                photoUploadEvents[3].subjectId
            )
        }
    }

    @Nested
    inner class saveUpdatePermissionsEvent {
        private lateinit var expectedEvent: AlbumEvent.UpdatePermissions

        @BeforeEach
        fun beforeEach() {
            val albumCreationEvent = albumCreationEvents[0]
            expectedEvent = AlbumEvent.UpdatePermissions(
                date = OffsetDateTime.now(),
                subjectId = albumCreationEvent.subjectId,
                number = nextAlbumEventNumber(),
                source = albumCreationEvent.source,
                content = AlbumEvent.UpdatePermissions.Content(
                    permissionsToAdd = setOf(
                        SubjectPermissions(
                            subjectId = userCreationEvents[2].subjectId,
                            permissions = EnumSet.allOf(Permission::class.java)
                        )
                    ),
                    permissionsToRemove = setOf(
                        SubjectPermissions(
                            subjectId = userCreationEvents[0].subjectId,
                            permissions = EnumSet.allOf(Permission::class.java)
                        ),
                        SubjectPermissions(
                            subjectId = userCreationEvents[1].subjectId,
                            permissions = setOf(Permission.Read)
                        )
                    )
                )
            )
        }

        @Test
        fun `should return created event`() {
            val event = albumEventRepo.saveUpdatePermissionsEvent(
                albumId = expectedEvent.subjectId,
                content = expectedEvent.content,
                source = expectedEvent.source
            )
            event shouldBe expectedEvent.copy(date = event.date)
            val user1Permissions = userAlbumAuthzRepo.fetch(userCreationEvents[0].subjectId, event.subjectId)
            user1Permissions!!.shouldBeEmpty()
            val user2Permissions = userAlbumAuthzRepo.fetch(userCreationEvents[1].subjectId, event.subjectId)
            user2Permissions!!.shouldBeEmpty()
            val user3Permissions = userAlbumAuthzRepo.fetch(userCreationEvents[2].subjectId, event.subjectId)
            user3Permissions shouldBe EnumSet.allOf(Permission::class.java)
        }
    }
}
