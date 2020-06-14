@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
internal class PhotoEventRepositoryImplTest : AbstractRepositoryTest() {
    @Nested
    inner class fetch {
        private lateinit var photoId: UUID
        private lateinit var source: EventSource.User

        @BeforeEach
        fun beforeEach() {
            val photoUploadEvent = photoUploadEvents[0]
            photoId = photoUploadEvent.subjectId
            source = userLocalSource(photoUploadEvent.source.id)
        }

        @Test
        fun `should return null if event does not exist`() {
            val event = photoEventRepo.fetch(nextPhotoEventNumber())
            event.shouldBeNull()
        }

        @Test
        fun `should return deletion event with number`() {
            val expectedEvent = photoEventRepo.saveDeletionEvent(photoId, source)
            val event = photoEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return transform event with number`() {
            val expectedEvent = photoEventRepo.saveTransformEvent(
                photoId = photoId,
                transform = Transform.Rotation(90.0),
                source = source
            )
            val event = photoEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return update event with number`() {
            val expectedEvent = photoEventRepo.saveUpdateEvent(
                photoId = photoId,
                content = PhotoEvent.Update.Content(
                    shootingDate = LocalDate.now()
                ),
                source = source
            )
            val event = photoEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return update permissions event with number`() {
            val expectedEvent = photoUpdatePermissionsEvents[0]
            val event = photoEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return upload event with number`() {
            val expectedEvent = photoUploadEvents[0]
            val event = photoEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }
    }

    @Nested
    inner class saveDeletionEvent {
        private lateinit var expectedEvent: PhotoEvent.Deletion

        @BeforeEach
        fun beforeEach() {
            val photoUploadEvent = photoUploadEvents[0]
            expectedEvent = PhotoEvent.Deletion(
                date = OffsetDateTime.now(),
                subjectId = photoUploadEvent.subjectId,
                number = nextPhotoEventNumber(),
                source = photoUploadEvent.source
            )
        }

        @Test
        fun `should return created event`() {
            val event = photoEventRepo.saveDeletionEvent(expectedEvent.subjectId, expectedEvent.source)
            event shouldBe expectedEvent.copy(date = event.date)
            photoRepo.fetchById(expectedEvent.subjectId).shouldBeNull()
        }
    }

    @Nested
    inner class saveTransformEvent {
        private lateinit var expectedPhoto: Photo
        private lateinit var expectedEvent: PhotoEvent.Transform

        @BeforeEach
        fun beforeEach() {
            val photoUploadEvent = photoUploadEvents[0]
            expectedEvent = PhotoEvent.Transform(
                date = OffsetDateTime.now(),
                subjectId = photoUploadEvent.subjectId,
                number = nextPhotoEventNumber(),
                source = photoUploadEvent.source,
                transform = Transform.Rotation(90.0)
            )
            expectedPhoto = photoUploadEvent.toPhoto(2)
        }

        @Test
        fun `should return created event`() {
            val event = photoEventRepo.saveTransformEvent(
                photoId = expectedEvent.subjectId,
                transform = expectedEvent.transform,
                source = expectedEvent.source
            )
            event shouldBe expectedEvent.copy(date = event.date)
            photoRepo.fetchById(expectedPhoto.id) shouldBe expectedPhoto
        }
    }

    @Nested
    inner class saveUpdateEvent {
        private lateinit var photoUploadEvent: PhotoEvent.Upload
        private lateinit var expectedDefaultEvent: PhotoEvent.Update
        private lateinit var expectedCompleteEvent: PhotoEvent.Update

        @BeforeEach
        fun beforeEach() {
            photoUploadEvent = photoUploadEvents[1]
            expectedDefaultEvent = PhotoEvent.Update(
                date = OffsetDateTime.now(),
                subjectId = photoUploadEvent.subjectId,
                number = nextPhotoEventNumber(),
                source = photoUploadEvent.source,
                content = PhotoEvent.Update.Content()
            )
            expectedCompleteEvent = expectedDefaultEvent.copy(
                content = expectedDefaultEvent.content.copy(
                    shootingDate = LocalDate.parse("2020-06-07")
                )
            )
        }

        @Test
        fun `should return created event (default)`() {
            val expectedPhoto = photoUploadEvent.toPhoto().copy(shootingDate = null)
            test(expectedDefaultEvent, expectedPhoto)
        }

        @Test
        fun `should return created event (complete)`() {
            val expectedPhoto = photoUploadEvent.toPhoto().copy(
                shootingDate = expectedCompleteEvent.content.shootingDate
            )
            test(expectedCompleteEvent, expectedPhoto)
        }

        private fun test(expectedEvent: PhotoEvent.Update, expectedPhoto: Photo) {
            val event = photoEventRepo.saveUpdateEvent(
                photoId = expectedEvent.subjectId,
                content = expectedEvent.content,
                source = expectedEvent.source
            )
            event shouldBe expectedEvent.copy(date = event.date)
            photoRepo.fetchById(expectedEvent.subjectId) shouldBe expectedPhoto
        }
    }

    @Nested
    inner class saveUpdatePermissionsEvent {
        private lateinit var expectedEvent: PhotoEvent.UpdatePermissions

        @BeforeEach
        fun beforeEach() {
            val photoUploadEvent = photoUploadEvents[0]
            expectedEvent = PhotoEvent.UpdatePermissions(
                date = OffsetDateTime.now(),
                subjectId = photoUploadEvent.subjectId,
                number = nextPhotoEventNumber(),
                source = photoUploadEvent.source,
                content = PhotoEvent.UpdatePermissions.Content(
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
            val event = photoEventRepo.saveUpdatePermissionsEvent(
                photoId = expectedEvent.subjectId,
                content = expectedEvent.content,
                source = expectedEvent.source
            )
            event shouldBe expectedEvent.copy(date = event.date)
            val user1Permissions = userPhotoAuthzRepo.fetch(userCreationEvents[0].subjectId, event.subjectId)
            user1Permissions!!.shouldBeEmpty()
            val user2Permissions = userPhotoAuthzRepo.fetch(userCreationEvents[1].subjectId, event.subjectId)
            user2Permissions!!.shouldBeEmpty()
            val user3Permissions = userPhotoAuthzRepo.fetch(userCreationEvents[2].subjectId, event.subjectId)
            user3Permissions shouldBe EnumSet.allOf(Permission::class.java)
        }
    }

    @Nested
    inner class saveUploadEvent {
        private lateinit var event: PhotoEvent.Upload

        @BeforeEach
        fun beforeEach() {
            event = photoUploadEvents[0]
        }

        @Test
        fun `should save photo`() {
            photoRepo.fetchById(event.subjectId) shouldBe event.toPhoto()
            val permissions = userPhotoAuthzRepo.fetch(event.source.id, event.subjectId)
            permissions shouldBe Permission.values().toSet()
        }
    }
}
