@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.EventSource
import io.ivana.core.PersonEvent
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
internal class PersonEventRepositoryImplTest : AbstractRepositoryTest() {
    @Nested
    inner class fetch {
        private lateinit var personId: UUID
        private lateinit var source: EventSource.User

        @BeforeEach
        fun beforeEach() {
            val personCreationEvent = personCreationEvents[0]
            personId = personCreationEvent.subjectId
            source = userLocalSource(personCreationEvent.source.id)
        }

        @Test
        fun `should return null if event does not exist`() {
            val event = personEventRepo.fetch(nextPersonEventNumber())
            event.shouldBeNull()
        }

        @Test
        fun `should return creation event with number`() {
            val expectedEvent = personCreationEvents[0]
            val event = personEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return deletion event with number`() {
            val expectedEvent = personEventRepo.saveDeletionEvent(personId, source)
            val event = personEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return update event with number`() {
            val expectedEvent = personEventRepo.saveUpdateEvent(
                personId = personId,
                content = PersonEvent.Update.Content(
                    lastName = "Leroy",
                    firstName = "Valère"
                ),
                source = source
            )
            val event = personEventRepo.fetch(expectedEvent.number)
            event shouldBe expectedEvent
        }
    }

    @Nested
    inner class saveCreationEvent {
        private lateinit var event: PersonEvent.Creation

        @BeforeEach
        fun beforeEach() {
            event = personCreationEvents[0]
        }

        @Test
        fun `should save person`() {
            personRepo.fetchById(event.subjectId) shouldBe event.toPerson()
        }
    }

    @Nested
    inner class saveDeletionEvent {
        private lateinit var expectedEvent: PersonEvent.Deletion

        @BeforeEach
        fun beforeEach() {
            val personCreationEvent = personCreationEvents[0]
            expectedEvent = PersonEvent.Deletion(
                date = OffsetDateTime.now(),
                subjectId = personCreationEvent.subjectId,
                number = nextPersonEventNumber(),
                source = personCreationEvent.source
            )
        }

        @Test
        fun `should return created event`() {
            val event = personEventRepo.saveDeletionEvent(expectedEvent.subjectId, expectedEvent.source)
            event shouldBe expectedEvent.copy(date = event.date)
            personRepo.fetchById(expectedEvent.subjectId).shouldBeNull()
        }
    }

    @Nested
    inner class saveUpdateEvent {
        private lateinit var personCreationEvent: PersonEvent.Creation
        private lateinit var expectedEvent: PersonEvent.Update

        @BeforeEach
        fun beforeEach() {
            personCreationEvent = personCreationEvents[1]
            expectedEvent = PersonEvent.Update(
                date = OffsetDateTime.now(),
                subjectId = personCreationEvent.subjectId,
                number = nextPersonEventNumber(),
                source = personCreationEvent.source,
                content = PersonEvent.Update.Content(
                    lastName = "Baisse",
                    firstName = "Eren"
                )
            )
        }

        @Test
        fun `should return created event`() {
            val expectedPerson = personCreationEvent.toPerson().copy(
                lastName = expectedEvent.content.lastName,
                firstName = expectedEvent.content.firstName
            )
            val event = personEventRepo.saveUpdateEvent(
                personId = expectedEvent.subjectId,
                content = expectedEvent.content,
                source = expectedEvent.source
            )
            event shouldBe expectedEvent.copy(date = event.date)
            personRepo.fetchById(expectedEvent.subjectId) shouldBe expectedPerson
        }
    }
}
