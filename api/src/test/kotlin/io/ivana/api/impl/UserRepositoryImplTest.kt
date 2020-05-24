@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.User
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
internal class UserRepositoryImplTest : AbstractRepositoryTest() {
    @Nested
    inner class count {
        @Test
        fun `should return count of photos of user`() {
            val count = userRepo.count()
            count shouldBe userCreationEvents.size
        }
    }

    @Nested
    inner class existsById {
        private lateinit var userId: UUID

        @BeforeEach
        fun beforeEach() {
            userId = userCreationEvents[0].subjectId
        }

        @Test
        fun `should return false if user does not exist`() {
            val exists = userRepo.existsById(UUID.randomUUID())
            exists.shouldBeFalse()
        }

        @Test
        fun `should return true if user exists`() {
            val exists = userRepo.existsById(userId)
            exists.shouldBeTrue()
        }
    }

    @Nested
    inner class fetchAll {
        private lateinit var createdUsers: List<User>

        @BeforeEach
        fun beforeEach() {
            createdUsers = userCreationEvents
                .map { it.toUser() }
                .sortedBy { it.id.toString() }
        }

        @Test
        fun `should return all users in interval`() {
            val users = userRepo.fetchAll(1, 10)
            users shouldBe createdUsers.subList(1, createdUsers.size)
        }
    }

    @Nested
    inner class fetchAllByIds {
        private lateinit var createdUsers: List<User>

        @BeforeEach
        fun beforeEach() {
            createdUsers = userCreationEvents.map { it.toUser() }
        }

        @Test
        fun `should return empty set if ids is empty`() {
            val users = userRepo.fetchAllByIds(emptySet())
            users.shouldBeEmpty()
        }

        @Test
        fun `should return all users`() {
            val users = userRepo.fetchAllByIds(createdUsers.map { it.id }.toSet())
            users shouldContainExactlyInAnyOrder createdUsers
        }
    }

    @Nested
    inner class fetchById {
        private lateinit var createdUser: User

        @BeforeEach
        fun beforeEach() {
            createdUser = userCreationEvents[0].toUser()
        }

        @Test
        fun `should return null if user does not exist`() {
            val user = userRepo.fetchById(UUID.randomUUID())
            user.shouldBeNull()
        }

        @Test
        fun `should return user with id`() {
            val user = userRepo.fetchById(createdUser.id)
            user shouldBe createdUser
        }
    }

    @Nested
    inner class fetchByName {
        private lateinit var createdUser: User

        @BeforeEach
        fun beforeEach() {
            createdUser = userCreationEvents[0].toUser()
        }

        @Test
        fun `should return null if user does not exist`() {
            val user = userRepo.fetchByName("gleroy")
            user.shouldBeNull()
        }

        @Test
        fun `should return user with name`() {
            val user = userRepo.fetchByName(createdUser.name)
            user shouldBe createdUser
        }
    }

    @Nested
    inner class fetchExistingIds {
        private lateinit var expectedExistingIds: Set<UUID>

        @BeforeEach
        fun beforeEach() {
            expectedExistingIds = userCreationEvents.map { it.subjectId }.toSet()
        }

        @Test
        fun `should return existing ids`() {
            val existingIds = userRepo.fetchExistingIds(expectedExistingIds + setOf(UUID.randomUUID()))
            existingIds shouldBe expectedExistingIds
        }
    }
}
