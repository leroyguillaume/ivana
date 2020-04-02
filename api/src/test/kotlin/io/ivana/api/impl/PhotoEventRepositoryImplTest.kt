@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.api.security.Permission
import io.ivana.api.security.UserPhotoAuthorizationRepository
import io.ivana.core.*
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
internal class PhotoEventRepositoryImplTest {
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
    private lateinit var repo: PhotoEventRepositoryImpl

    @Autowired
    private lateinit var authRepo: UserPhotoAuthorizationRepository

    private lateinit var creationEvent: UserEvent.Creation
    private lateinit var createdUser: User

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        creationEvent = userEventRepo.saveCreationEvent(userCreationEventContent, EventSource.System)
        createdUser = User(
            id = creationEvent.subjectId,
            name = creationEvent.content.name,
            hashedPwd = creationEvent.content.hashedPwd,
            role = creationEvent.content.role
        )
    }

    @Nested
    inner class fetch {
        private val subjectId = UUID.randomUUID()
        private val number = 1L

        @Test
        fun `should return null if event does not exist`() {
            val event = repo.fetch(subjectId, number)
            event.shouldBeNull()
        }

        @Test
        fun `should return upload event with subject id and number`() {
            val expectedEvent = repo.saveUploadEvent(
                content = PhotoEvent.Upload.Content(
                    type = Photo.Type.Jpg,
                    hash = "hash"
                ),
                source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1"))
            )
            val event = repo.fetch(expectedEvent.subjectId, expectedEvent.number)
            event shouldBe expectedEvent
        }
    }

    @Nested
    inner class saveUploadEvent {
        private lateinit var expectedEvent: PhotoEvent.Upload

        @BeforeEach
        fun beforeEach() {
            expectedEvent = UUID.randomUUID().let { id ->
                PhotoEvent.Upload(
                    date = OffsetDateTime.now(),
                    subjectId = id,
                    source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1")),
                    content = PhotoEvent.Upload.Content(
                        type = Photo.Type.Jpg,
                        hash = "hash"
                    )
                )
            }
        }

        @Test
        fun `should return created event`() {
            val event = repo.saveUploadEvent(expectedEvent.content, expectedEvent.source)
            event shouldBe expectedEvent.copy(
                date = event.date,
                subjectId = event.subjectId
            )
            val permissions = authRepo.fetch(createdUser.id, event.subjectId)
            permissions shouldContainExactly Permission.values().toSet()
        }
    }
}
