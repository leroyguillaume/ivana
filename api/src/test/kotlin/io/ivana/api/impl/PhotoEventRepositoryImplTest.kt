@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
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
internal class PhotoEventRepositoryImplTest {
    private val pwdEncoder = BCryptPasswordEncoder()
    private val ownerCreationEventContent = UserEvent.Creation.Content(
        name = "admin",
        hashedPwd = pwdEncoder.encode("changeit"),
        role = Role.SuperAdmin
    )
    private val user1CreationEventContent = UserEvent.Creation.Content(
        name = "user1",
        hashedPwd = pwdEncoder.encode("changeit"),
        role = Role.User
    )

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var userEventRepo: UserEventRepository

    @Autowired
    private lateinit var repo: PhotoEventRepositoryImpl

    @Autowired
    private lateinit var photoRepo: PhotoRepositoryImpl

    @Autowired
    private lateinit var authzRepo: UserPhotoAuthorizationRepositoryImpl

    private lateinit var owner: User
    private lateinit var user: User

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        owner = userEventRepo.saveCreationEvent(ownerCreationEventContent, EventSource.System).toUser()
        user = userEventRepo.saveCreationEvent(user1CreationEventContent, EventSource.System).toUser()
    }

    @Nested
    inner class fetch {
        private val number = 1L

        @Test
        fun `should return null if event does not exist`() {
            val event = repo.fetch(number)
            event.shouldBeNull()
        }

        @Test
        fun `should return deletion event with number`() {
            val expectedEvent = repo.saveDeletionEvent(
                photoId = UUID.randomUUID(),
                source = EventSource.User(owner.id, InetAddress.getByName("127.0.0.1"))
            )
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return transform event with number`() {
            val expectedEvent = repo.saveTransformEvent(
                photoId = UUID.randomUUID(),
                transform = Transform.Rotation(90.0),
                source = EventSource.User(owner.id, InetAddress.getByName("127.0.0.1"))
            )
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return update event with number`() {
            val uploadEvent = repo.saveUploadEvent(
                content = PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash"
                ),
                source = EventSource.User(owner.id, InetAddress.getByName("127.0.0.1"))
            )
            val expectedEvent = repo.saveUpdatePermissionsEvent(
                photoId = uploadEvent.subjectId,
                content = PhotoEvent.UpdatePermissions.Content(
                    permissionsToAdd = setOf(
                        SubjectPermissions(
                            subjectId = user.id,
                            permissions = setOf(Permission.Read)
                        )
                    ),
                    permissionsToRemove = setOf(
                        SubjectPermissions(
                            subjectId = user.id,
                            permissions = setOf(Permission.Delete)
                        )
                    )
                ),
                source = EventSource.User(owner.id, InetAddress.getByName("127.0.0.1"))
            )
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return upload event with number`() {
            val expectedEvent = repo.saveUploadEvent(
                content = PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash"
                ),
                source = EventSource.User(owner.id, InetAddress.getByName("127.0.0.1"))
            )
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }
    }

    @Nested
    inner class saveDeletionEvent {
        private lateinit var expectedEvent: PhotoEvent.Deletion

        @BeforeEach
        fun beforeEach() {
            val uploadEvent = repo.saveUploadEvent(
                content = PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash"
                ),
                source = EventSource.User(owner.id, InetAddress.getByName("127.0.0.1"))
            )
            expectedEvent = PhotoEvent.Deletion(
                date = OffsetDateTime.now(),
                subjectId = uploadEvent.subjectId,
                number = 2,
                source = uploadEvent.source
            )
        }

        @Test
        fun `should return created event`() {
            val event = repo.saveDeletionEvent(expectedEvent.subjectId, expectedEvent.source)
            event shouldBe expectedEvent.copy(
                date = event.date,
                subjectId = event.subjectId
            )
            photoRepo.fetchById(expectedEvent.subjectId).shouldBeNull()
        }
    }

    @Nested
    inner class saveTransformEvent {
        private val uploadContent = PhotoEvent.Upload.Content(
            type = Photo.Type.Jpg,
            hash = "hash"
        )

        private lateinit var expectedPhoto: Photo
        private lateinit var expectedEvent: PhotoEvent.Transform

        @BeforeEach
        fun beforeEach() {
            val source = EventSource.User(owner.id, InetAddress.getByName("127.0.0.1"))
            expectedPhoto = repo.saveUploadEvent(uploadContent, source).toPhoto(1, 2)
            expectedEvent = PhotoEvent.Transform(
                date = OffsetDateTime.now(),
                subjectId = expectedPhoto.id,
                number = 2,
                source = source,
                transform = Transform.Rotation(90.0)
            )
        }

        @Test
        fun `should return created event`() {
            val event = repo.saveTransformEvent(expectedEvent.subjectId, expectedEvent.transform, expectedEvent.source)
            event shouldBe expectedEvent.copy(
                date = event.date,
                subjectId = event.subjectId
            )
            photoRepo.fetchById(expectedPhoto.id) shouldBe expectedPhoto
        }
    }

    @Nested
    inner class saveUpdatePermissionsEvent {
        private lateinit var expectedEvent: PhotoEvent.UpdatePermissions

        @BeforeEach
        fun beforeEach() {
            val uploadEvent = repo.saveUploadEvent(
                content = PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash"
                ),
                source = EventSource.User(owner.id, InetAddress.getByName("127.0.0.1"))
            )
            expectedEvent = PhotoEvent.UpdatePermissions(
                date = OffsetDateTime.now(),
                subjectId = uploadEvent.subjectId,
                number = 2,
                source = EventSource.User(owner.id, InetAddress.getByName("127.0.0.1")),
                content = PhotoEvent.UpdatePermissions.Content(
                    permissionsToAdd = setOf(
                        SubjectPermissions(
                            subjectId = user.id,
                            permissions = EnumSet.allOf(Permission::class.java)
                        )
                    ),
                    permissionsToRemove = setOf(
                        SubjectPermissions(
                            subjectId = owner.id,
                            permissions = EnumSet.allOf(Permission::class.java)
                        )
                    )
                )
            )
        }

        @Test
        fun `should return created event`() {
            val event =
                repo.saveUpdatePermissionsEvent(expectedEvent.subjectId, expectedEvent.content, expectedEvent.source)
            event shouldBe expectedEvent.copy(
                date = event.date,
                subjectId = event.subjectId
            )
            val ownerPermissions = authzRepo.fetch(owner.id, event.subjectId)
            ownerPermissions shouldBe emptySet()
            val userPermissions = authzRepo.fetch(user.id, event.subjectId)
            userPermissions shouldBe EnumSet.allOf(Permission::class.java)
        }
    }

    @Nested
    inner class saveUploadEvent {
        private lateinit var expectedEvent: PhotoEvent.Upload
        private lateinit var expectedPhoto: Photo

        @BeforeEach
        fun beforeEach() {
            expectedEvent = UUID.randomUUID().let { id ->
                PhotoEvent.Upload(
                    date = OffsetDateTime.now(),
                    subjectId = id,
                    number = 1,
                    source = EventSource.User(owner.id, InetAddress.getByName("127.0.0.1")),
                    content = PhotoEvent.Upload.Content(
                        type = Photo.Type.Jpg,
                        hash = "hash"
                    )
                )
            }
            expectedPhoto = Photo(
                id = expectedEvent.subjectId,
                ownerId = owner.id,
                uploadDate = expectedEvent.date,
                type = expectedEvent.content.type,
                hash = expectedEvent.content.hash,
                no = 1,
                version = 1
            )
        }

        @Test
        fun `should return created event`() {
            val event = repo.saveUploadEvent(expectedEvent.content, expectedEvent.source)
            event shouldBe expectedEvent.copy(
                date = event.date,
                subjectId = event.subjectId
            )
            photoRepo.fetchById(event.subjectId) shouldBe expectedPhoto.copy(
                id = event.subjectId,
                uploadDate = event.date
            )
            val permissions = authzRepo.fetch(owner.id, event.subjectId)
            permissions shouldBe Permission.values().toSet()
        }
    }
}
