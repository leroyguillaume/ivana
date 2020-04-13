@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
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
    private val creationEventContent = UserEvent.Creation.Content(
        name = "admin",
        hashedPwd = pwdEncoder.encode("changeit"),
        role = Role.SuperAdmin
    )

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var repo: UserRepositoryImpl

    @Autowired
    private lateinit var eventRepo: UserEventRepository

    private lateinit var creationEvent: UserEvent.Creation
    private lateinit var createdUser: User

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        creationEvent = eventRepo.saveCreationEvent(creationEventContent, EventSource.System)
        createdUser = User(
            id = creationEvent.subjectId,
            name = creationEvent.content.name,
            hashedPwd = creationEvent.content.hashedPwd,
            role = creationEvent.content.role
        )
    }

    @Nested
    inner class existsById {
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
    inner class fetchById {
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
}
