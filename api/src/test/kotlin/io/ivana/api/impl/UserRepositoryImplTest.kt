@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

@SpringBootTest
internal class UserRepositoryImplTest {
    private val pwdEncoder = BCryptPasswordEncoder()
    private val initData = listOf(
        UserEvent.Creation.Content(
            name = "user1",
            hashedPwd = pwdEncoder.encode("changeit"),
            role = Role.User
        ),
        UserEvent.Creation.Content(
            name = "user2",
            hashedPwd = pwdEncoder.encode("changeit"),
            role = Role.Admin
        ),
        UserEvent.Creation.Content(
            name = "user3",
            hashedPwd = pwdEncoder.encode("changeit"),
            role = Role.SuperAdmin
        )
    )

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var repo: UserRepositoryImpl

    @Autowired
    private lateinit var eventRepo: UserEventRepository

    private lateinit var createdUsers: List<User>

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        createdUsers = initData.map { eventRepo.saveCreationEvent(it, EventSource.System).toUser() }
    }

    @Nested
    inner class count {
        @Test
        fun `should return count of photos of user`() {
            val count = repo.count()
            count shouldBe createdUsers.size
        }
    }

    @Nested
    inner class existsById {
        private lateinit var createdUser: User

        @BeforeEach
        fun beforeEach() {
            createdUser = createdUsers[0]
        }

        @Test
        fun `should return false if user does not exist`() {
            val exists = repo.existsById(UUID.randomUUID())
            exists.shouldBeFalse()
        }

        @Test
        fun `should return true if user exists`() {
            val exists = repo.existsById(createdUser.id)
            exists.shouldBeTrue()
        }
    }

    @Nested
    inner class fetchAll {
        @Test
        fun `should return all users in interval`() {
            val users = repo.fetchAll(1, 10)
            users shouldBe createdUsers.sortedBy { it.id.toString() }.subList(1, createdUsers.size)
        }
    }

    @Nested
    inner class fetchAllByIds {
        @Test
        fun `should return empty set if ids is empty`() {
            val users = repo.fetchAllByIds(emptySet())
            users.shouldBeEmpty()
        }

        @Test
        fun `should return all users`() {
            val users = repo.fetchAllByIds(createdUsers.map { it.id }.toSet())
            users shouldContainExactlyInAnyOrder createdUsers
        }
    }

    @Nested
    inner class fetchById {
        private lateinit var createdUser: User

        @BeforeEach
        fun beforeEach() {
            createdUser = createdUsers[0]
        }

        @Test
        fun `should return null if user does not exist`() {
            val user = repo.fetchById(UUID.randomUUID())
            user.shouldBeNull()
        }

        @Test
        fun `should return user with id`() {
            val user = repo.fetchById(createdUser.id)
            user shouldBe createdUser
        }
    }

    @Nested
    inner class fetchByName {
        private lateinit var createdUser: User

        @BeforeEach
        fun beforeEach() {
            createdUser = createdUsers[0]
        }

        @Test
        fun `should return null if user does not exist`() {
            val user = repo.fetchByName("gleroy")
            user.shouldBeNull()
        }

        @Test
        fun `should return user with name`() {
            val user = repo.fetchByName(createdUser.name)
            user shouldBe createdUser
        }
    }

    @Nested
    inner class fetchExistingIds {
        private lateinit var expectedExistingIds: Set<UUID>

        @BeforeEach
        fun beforeEach() {
            expectedExistingIds = createdUsers.map { it.id }.toSet()
        }

        @Test
        fun `should return existing ids`() {
            val existingIds = repo.fetchExistingIds(expectedExistingIds + setOf(UUID.randomUUID()))
            existingIds shouldBe expectedExistingIds
        }
    }
}
