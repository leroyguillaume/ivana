@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.Person
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
internal class PersonRepositoryImplTest : AbstractRepositoryTest() {
    @Nested
    inner class count {
        @Test
        fun `should return count of people`() {
            val count = personRepo.count()
            count shouldBe personCreationEvents.size
        }
    }

    @Nested
    inner class existsById {
        private lateinit var personId: UUID

        @BeforeEach
        fun beforeEach() {
            personId = personCreationEvents[0].subjectId
        }

        @Test
        fun `should return false if person does not exist`() {
            val exists = personRepo.existsById(UUID.randomUUID())
            exists.shouldBeFalse()
        }

        @Test
        fun `should return true if person exists`() {
            val exists = personRepo.existsById(personId)
            exists.shouldBeTrue()
        }
    }

    @Nested
    inner class fetchAll {
        private lateinit var createdPeople: List<Person>

        @BeforeEach
        fun beforeEach() {
            createdPeople = personCreationEvents
                .map { it.toPerson() }
                .sortedBy { it.id.toString() }
        }

        @Test
        fun `should return all people in interval`() {
            val people = personRepo.fetchAll(1, 10)
            people shouldBe createdPeople.subList(1, createdPeople.size)
        }
    }

    @Nested
    inner class fetchAllByIds {
        private lateinit var createdPeople: List<Person>

        @BeforeEach
        fun beforeEach() {
            createdPeople = personCreationEvents.map { it.toPerson() }
        }

        @Test
        fun `should return empty set if ids is empty`() {
            val people = personRepo.fetchAllByIds(emptySet())
            people.shouldBeEmpty()
        }

        @Test
        fun `should return all people`() {
            val people = personRepo.fetchAllByIds(createdPeople.map { it.id }.toSet())
            people shouldContainExactlyInAnyOrder createdPeople
        }
    }

    @Nested
    inner class fetchById {
        private lateinit var expectedPerson: Person

        @BeforeEach
        fun beforeEach() {
            expectedPerson = personCreationEvents[0].toPerson()
        }

        @Test
        fun `should return null if person does not exist`() {
            val person = personRepo.fetchById(UUID.randomUUID())
            person.shouldBeNull()
        }

        @Test
        fun `should return person with id`() {
            val person = personRepo.fetchById(expectedPerson.id)
            person shouldBe expectedPerson
        }
    }

    @Nested
    inner class fetchByName {
        private lateinit var expectedPerson: Person

        @BeforeEach
        fun beforeEach() {
            expectedPerson = personCreationEvents[0].toPerson()
        }

        @Test
        fun `should return null if person does not exist`() {
            val person = personRepo.fetchByName(expectedPerson.lastName, "")
            person.shouldBeNull()
        }

        @Test
        fun `should return person with id`() {
            val person = personRepo.fetchByName(expectedPerson.lastName, expectedPerson.firstName)
            person shouldBe expectedPerson
        }
    }

    @Nested
    inner class fetchExistingIds {
        private lateinit var expectedExistingIds: Set<UUID>

        @BeforeEach
        fun beforeEach() {
            expectedExistingIds = personCreationEvents.map { it.subjectId }.toSet()
        }

        @Test
        fun `should return existing ids`() {
            val existingIds = personRepo.fetchExistingIds(expectedExistingIds + setOf(UUID.randomUUID()))
            existingIds shouldBe expectedExistingIds
        }
    }

    @Nested
    inner class suggest {
        @Test
        fun `should return empty list`() {
            personRepo.suggest("toto", 10).shouldBeEmpty()
        }

        @Test
        fun `should return person1`() {
            personRepo.suggest("guillaume leroy", 10) shouldContainExactly listOf(personCreationEvents[0].toPerson())
        }

        @Test
        fun `should return person2 and person1`() {
            personRepo.suggest("ero", 2) shouldContainExactly listOf(
                personCreationEvents[1].toPerson(),
                personCreationEvents[0].toPerson()
            )
        }
    }
}
