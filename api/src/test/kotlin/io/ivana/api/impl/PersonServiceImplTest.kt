@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*

internal class PersonServiceImplTest {
    private lateinit var personRepo: PersonRepository
    private lateinit var personEventRepo: PersonEventRepository
    private lateinit var service: PersonServiceImpl

    @BeforeEach
    fun beforeEach() {
        personRepo = mockk()
        personEventRepo = mockk()

        service = PersonServiceImpl(personRepo, personEventRepo)
    }

    @Nested
    inner class create {
        private val creationEvent = PersonEvent.Creation(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            content = PersonEvent.Creation.Content(
                lastName = "Leroy",
                firstName = "Guillaume"
            )
        )
        private val expectedPerson = Person(
            id = creationEvent.subjectId,
            lastName = creationEvent.content.lastName,
            firstName = creationEvent.content.firstName
        )

        @Test
        fun `should throw exception if name is already used`() {
            every { personRepo.fetchByName(expectedPerson.lastName, expectedPerson.firstName) } returns expectedPerson
            val exception = assertThrows<PersonAlreadyExistsException> {
                service.create(creationEvent.content, creationEvent.source)
            }
            exception.person shouldBe expectedPerson
            verify { personRepo.fetchByName(expectedPerson.lastName, expectedPerson.firstName) }
            confirmVerified(personRepo)
        }

        @Test
        fun `should save creation event`() {
            every { personRepo.fetchByName(expectedPerson.lastName, expectedPerson.firstName) } returns null
            every {
                personEventRepo.saveCreationEvent(creationEvent.content, creationEvent.source)
            } returns creationEvent
            every { personRepo.fetchById(expectedPerson.id) } returns expectedPerson
            val person = service.create(creationEvent.content, creationEvent.source)
            person shouldBe expectedPerson
            verify { personRepo.fetchByName(expectedPerson.lastName, expectedPerson.firstName) }
            verify { personEventRepo.saveCreationEvent(creationEvent.content, creationEvent.source) }
            verify { personRepo.fetchById(expectedPerson.id) }
            confirmVerified(personRepo, personEventRepo)
        }
    }

    @Nested
    inner class delete {
        private val event = PersonEvent.Deletion(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1"))
        )

        @Test
        fun `should throw exception if person does not exist`() {
            every { personRepo.existsById(event.subjectId) } returns false
            val exception = assertThrows<EntityNotFoundException> { service.delete(event.subjectId, event.source) }
            exception shouldHaveMessage "Person ${event.subjectId} does not exist"
            verify { personRepo.existsById(event.subjectId) }
            confirmVerified(personRepo)
        }

        @Test
        fun `should delete person`() {
            every { personRepo.existsById(event.subjectId) } returns true
            every { personEventRepo.saveDeletionEvent(event.subjectId, event.source) } returns event
            service.delete(event.subjectId, event.source)
            verify { personRepo.existsById(event.subjectId) }
            verify { personEventRepo.saveDeletionEvent(event.subjectId, event.source) }
            confirmVerified(personRepo)
        }
    }

    @Nested
    inner class getAll {
        private val pageNo = 1
        private val pageSize = 3
        private val expectedPage = Page(
            content = listOf(
                Person(
                    id = UUID.randomUUID(),
                    lastName = "Leroy",
                    firstName = "Guillaume"
                ),
                Person(
                    id = UUID.randomUUID(),
                    lastName = "Leroy",
                    firstName = "Annie"
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )

        @Test
        fun `should return page`() {
            every { personRepo.fetchAll(pageNo - 1, pageSize) } returns expectedPage.content
            every { personRepo.count() } returns 2
            val page = service.getAll(pageNo, pageSize)
            page shouldBe expectedPage
            verify { personRepo.fetchAll(pageNo - 1, pageSize) }
            verify { personRepo.count() }
            confirmVerified(personRepo)
        }
    }

    @Nested
    inner class getAllByIds {
        private val expectedPersons = setOf(
            Person(
                id = UUID.randomUUID(),
                lastName = "Leroy",
                firstName = "Guillaume"
            ),
            Person(
                id = UUID.randomUUID(),
                lastName = "Leroy",
                firstName = "Annie"
            )
        )
        private val ids = expectedPersons.map { it.id }.toSet()

        @Test
        fun `should throw exception if entities not found`() {
            every { personRepo.fetchAllByIds(ids) } returns emptySet()
            val exception = assertThrows<ResourcesNotFoundException> { service.getAllByIds(ids) }
            exception.ids shouldBe ids
            verify { personRepo.fetchAllByIds(ids) }
            confirmVerified(personRepo)
        }

        @Test
        fun `should return all people`() {
            every { personRepo.fetchAllByIds(ids) } returns expectedPersons
            val people = service.getAllByIds(ids)
            people shouldBe expectedPersons
            verify { personRepo.fetchAllByIds(ids) }
            confirmVerified(personRepo)
        }
    }

    @Nested
    inner class getById {
        private val expectedPerson = Person(
            id = UUID.randomUUID(),
            lastName = "Leroy",
            firstName = "Guillaume"
        )

        @Test
        fun `should throw exception if person does not exist`() {
            every { personRepo.fetchById(expectedPerson.id) } returns null
            val exception = assertThrows<EntityNotFoundException> { service.getById(expectedPerson.id) }
            exception shouldHaveMessage "Person ${expectedPerson.id} does not exist"
            verify { personRepo.fetchById(expectedPerson.id) }
            confirmVerified(personRepo)
        }

        @Test
        fun `should return person with id`() {
            every { personRepo.fetchById(expectedPerson.id) } returns expectedPerson
            val person = service.getById(expectedPerson.id)
            person shouldBe expectedPerson
            verify { personRepo.fetchById(person.id) }
            confirmVerified(personRepo)
        }
    }

    @Nested
    inner class suggest {
        private val expectedPeople = listOf(
            Person(
                id = UUID.randomUUID(),
                lastName = "Leroy",
                firstName = "Guillaume"
            ),
            Person(
                id = UUID.randomUUID(),
                lastName = "Leroy",
                firstName = "Annie"
            )
        )
        private val name = "ero"
        private val count = 2

        @Test
        fun `should return suggested people`() {
            every { personRepo.suggest(name, count) } returns expectedPeople
            val people = service.suggest(name, count)
            people shouldBe expectedPeople
            verify { personRepo.suggest(name, count) }
            confirmVerified(personRepo)
        }
    }

    @Nested
    inner class update {
        private val event = PersonEvent.Update(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            content = PersonEvent.Update.Content(
                lastName = "Leroy",
                firstName = "Guillaume"
            )
        )
        private val person = Person(
            id = event.subjectId,
            lastName = event.content.lastName,
            firstName = event.content.firstName
        )
        private val existingPerson = person.copy(id = UUID.randomUUID())

        @Test
        fun `should throw exception if person does not exist`() {
            every { personRepo.existsById(event.subjectId) } returns false
            val exception = assertThrows<EntityNotFoundException> {
                service.update(event.subjectId, event.content, event.source)
            }
            exception shouldHaveMessage "Person ${event.subjectId} does not exist"
            verify { personRepo.existsById(event.subjectId) }
            confirmVerified(personRepo)
        }

        @Test
        fun `should throw exception if name is already used`() {
            every { personRepo.existsById(event.subjectId) } returns true
            every { personRepo.fetchByName(event.content.lastName, event.content.firstName) } returns existingPerson
            val exception = assertThrows<PersonAlreadyExistsException> {
                service.update(event.subjectId, event.content, event.source)
            }
            exception.person shouldBe existingPerson
            verify { personRepo.existsById(event.subjectId) }
            verify { personRepo.fetchByName(event.content.lastName, event.content.firstName) }
            confirmVerified(personRepo)
        }

        @Test
        fun `should update person (no change)`() {
            every { personRepo.existsById(event.subjectId) } returns true
            every { personRepo.fetchByName(event.content.lastName, event.content.firstName) } returns person
            every { personEventRepo.saveUpdateEvent(event.subjectId, event.content, event.source) } returns event
            every { personRepo.fetchById(event.subjectId) } returns person
            service.update(event.subjectId, event.content, event.source)
            verify { personRepo.existsById(event.subjectId) }
            verify { personRepo.fetchByName(event.content.lastName, event.content.firstName) }
            verify { personEventRepo.saveUpdateEvent(event.subjectId, event.content, event.source) }
            verify { personRepo.fetchById(event.subjectId) }
            confirmVerified(personRepo, personEventRepo)
        }

        @Test
        fun `should update person`() {
            every { personRepo.existsById(event.subjectId) } returns true
            every { personRepo.fetchByName(event.content.lastName, event.content.firstName) } returns null
            every { personEventRepo.saveUpdateEvent(event.subjectId, event.content, event.source) } returns event
            every { personRepo.fetchById(event.subjectId) } returns person
            service.update(event.subjectId, event.content, event.source)
            verify { personRepo.existsById(event.subjectId) }
            verify { personRepo.fetchByName(event.content.lastName, event.content.firstName) }
            verify { personEventRepo.saveUpdateEvent(event.subjectId, event.content, event.source) }
            verify { personRepo.fetchById(event.subjectId) }
            confirmVerified(personRepo, personEventRepo)
        }
    }
}
