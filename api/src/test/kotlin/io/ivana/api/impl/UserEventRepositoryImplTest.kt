@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.EventSource
import io.ivana.core.Role
import io.ivana.core.User
import io.ivana.core.UserEvent
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
internal class UserEventRepositoryImplTest {
    private val pwdEncoder = BCryptPasswordEncoder()

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var repo: UserEventRepositoryImpl

    @Autowired
    private lateinit var userRepo: UserRepositoryImpl

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
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
                content = UserEvent.Creation.Content(
                    name = "admin",
                    hashedPwd = pwdEncoder.encode("changeit"),
                    role = Role.SuperAdmin
                ),
                source = EventSource.System
            )
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return deletion event with subject id and number`() {
            val expectedEvent = repo.saveDeletionEvent(UUID.randomUUID(), EventSource.System)
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return login event with subject id and number`() {
            val expectedEvent = repo.saveLoginEvent(EventSource.User(subjectId, InetAddress.getByName("127.0.0.1")))
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return password update event with subject id and number`() {
            val expectedEvent = repo.savePasswordUpdateEvent(UUID.randomUUID(), "newHashedPwd", EventSource.System)
            val event = repo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }
    }

    @Nested
    inner class saveCreationEvent {
        private val expectedEvent = UUID.randomUUID().let { id ->
            UserEvent.Creation(
                date = OffsetDateTime.now(),
                subjectId = id,
                number = 1,
                source = EventSource.System,
                content = UserEvent.Creation.Content(
                    name = "admin",
                    hashedPwd = pwdEncoder.encode("changeit"),
                    role = Role.SuperAdmin
                )
            )
        }
        private val expectedUser = User(
            id = UUID.randomUUID(),
            name = expectedEvent.content.name,
            hashedPwd = expectedEvent.content.hashedPwd,
            role = expectedEvent.content.role,
            creationDate = expectedEvent.date
        )

        @Test
        fun `should return created event`() {
            val event = repo.saveCreationEvent(expectedEvent.content, expectedEvent.source)
            event shouldBe expectedEvent.copy(
                date = event.date,
                subjectId = event.subjectId
            )
            userRepo.fetchById(event.subjectId) shouldBe expectedUser.copy(
                id = event.subjectId,
                creationDate = event.date
            )
        }
    }

    @Nested
    inner class saveDeletionEvent {
        private lateinit var expectedEvent: UserEvent.Deletion

        @BeforeEach
        fun beforeEach() {
            val creationEvent = repo.saveCreationEvent(
                content = UserEvent.Creation.Content(
                    name = "admin",
                    hashedPwd = "hashedPwd",
                    role = Role.SuperAdmin
                ),
                source = EventSource.System
            )
            expectedEvent = UserEvent.Deletion(
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
            userRepo.fetchById(expectedEvent.subjectId).shouldBeNull()
        }
    }

    @Nested
    inner class saveLoginEvent {
        private val expectedEvent = UUID.randomUUID().let { id ->
            UserEvent.Login(
                date = OffsetDateTime.now(),
                subjectId = id,
                number = 1,
                source = EventSource.User(id, InetAddress.getByName("127.0.0.1"))
            )
        }

        @Test
        fun `should return created event`() {
            val event = repo.saveLoginEvent(expectedEvent.source)
            event shouldBe expectedEvent.copy(date = event.date)
        }
    }

    @Nested
    inner class savePasswordUpdateEvent {
        private val creationEventContent = UserEvent.Creation.Content(
            name = "admin",
            hashedPwd = pwdEncoder.encode("changeit"),
            role = Role.SuperAdmin
        )

        private lateinit var creationEvent: UserEvent.Creation
        private lateinit var expectedEvent: UserEvent.PasswordUpdate

        @BeforeEach
        fun beforeEach() {
            creationEvent = repo.saveCreationEvent(creationEventContent, EventSource.System)
            expectedEvent = UserEvent.PasswordUpdate(
                source = EventSource.System,
                subjectId = creationEvent.subjectId,
                date = OffsetDateTime.now(),
                number = 2,
                newHashedPwd = pwdEncoder.encode("foo123")
            )
        }

        @Test
        fun `should return created event`() {
            val event = repo.savePasswordUpdateEvent(
                userId = expectedEvent.subjectId,
                newHashedPwd = expectedEvent.newHashedPwd,
                source = expectedEvent.source
            )
            event shouldBe expectedEvent.copy(date = event.date)
            userRepo.fetchById(expectedEvent.subjectId)!!.hashedPwd shouldBe expectedEvent.newHashedPwd
        }
    }
}
