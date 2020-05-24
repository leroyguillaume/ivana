@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.EventSource
import io.ivana.core.UserEvent
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
internal class UserEventRepositoryImplTest : AbstractRepositoryTest() {
    @Nested
    inner class fetch {
        private lateinit var userId: UUID

        @BeforeEach
        fun beforeEach() {
            userId = userCreationEvents[0].subjectId
        }

        @Test
        fun `should return null if event does not exist`() {
            val event = userEventRepo.fetch(nextUserEventNumber())
            event.shouldBeNull()
        }

        @Test
        fun `should return creation event with number`() {
            val expectedEvent = userCreationEvents[0]
            val event = userEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return deletion event with number`() {
            val expectedEvent = userEventRepo.saveDeletionEvent(userId, EventSource.System)
            val event = userEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return login event with number`() {
            val expectedEvent = userEventRepo.saveLoginEvent(userLocalSource(userId))
            val event = userEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return password update event with number`() {
            val expectedEvent = userEventRepo.savePasswordUpdateEvent(userId, "newHashedPwd", EventSource.System)
            val event = userEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }
    }

    @Nested
    inner class saveCreationEvent {
        private lateinit var event: UserEvent.Creation

        @BeforeEach
        fun beforeEach() {
            event = userCreationEvents[0]
        }

        @Test
        fun `should save user`() {
            userRepo.fetchById(event.subjectId) shouldBe event.toUser()
        }
    }

    @Nested
    inner class saveDeletionEvent {
        private lateinit var expectedEvent: UserEvent.Deletion

        @BeforeEach
        fun beforeEach() {
            val userId = userCreationEvents[0].subjectId
            expectedEvent = UserEvent.Deletion(
                date = OffsetDateTime.now(),
                subjectId = userId,
                number = nextUserEventNumber(),
                source = userLocalSource(userId)
            )
        }

        @Test
        fun `should return created event`() {
            val event = userEventRepo.saveDeletionEvent(expectedEvent.subjectId, expectedEvent.source)
            event shouldBe expectedEvent.copy(date = event.date)
            userRepo.fetchById(expectedEvent.subjectId).shouldBeNull()
        }
    }

    @Nested
    inner class saveLoginEvent {
        private lateinit var expectedEvent: UserEvent.Login

        @BeforeEach
        fun beforeEach() {
            val userId = userCreationEvents[0].subjectId
            expectedEvent = UserEvent.Login(
                date = OffsetDateTime.now(),
                subjectId = userId,
                number = nextUserEventNumber(),
                source = userLocalSource(userId)
            )
        }

        @Test
        fun `should return created event`() {
            val event = userEventRepo.saveLoginEvent(expectedEvent.source)
            event shouldBe expectedEvent.copy(date = event.date)
        }
    }

    @Nested
    inner class savePasswordUpdateEvent {
        private lateinit var expectedEvent: UserEvent.PasswordUpdate

        @BeforeEach
        fun beforeEach() {
            val userId = userCreationEvents[0].subjectId
            expectedEvent = UserEvent.PasswordUpdate(
                source = EventSource.System,
                subjectId = userId,
                date = OffsetDateTime.now(),
                number = nextUserEventNumber(),
                newHashedPwd = "newHashedPwd"
            )
        }

        @Test
        fun `should return created event`() {
            val event = userEventRepo.savePasswordUpdateEvent(
                userId = expectedEvent.subjectId,
                newHashedPwd = expectedEvent.newHashedPwd,
                source = expectedEvent.source
            )
            event shouldBe expectedEvent.copy(date = event.date)
            userRepo.fetchById(expectedEvent.subjectId)!!.hashedPwd shouldBe expectedEvent.newHashedPwd
        }
    }
}
