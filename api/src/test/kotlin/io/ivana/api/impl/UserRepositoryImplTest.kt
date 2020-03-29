@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.EventSource
import io.ivana.core.User
import io.ivana.core.UserEvent
import io.ivana.core.UserEventRepository
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
        hashedPwd = pwdEncoder.encode("changeit")
    )

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    private lateinit var repo: UserRepositoryImpl
    private lateinit var eventRepo: UserEventRepository
    private lateinit var creationEvent: UserEvent.Creation
    private lateinit var createdUser: User

    @BeforeEach
    fun beforeEach() {
        eventRepo = UserEventRepositoryImpl(jdbc)
        repo = UserRepositoryImpl(jdbc)

        cleanDb(jdbc)
        creationEvent = eventRepo.saveCreationEvent(creationEventContent, EventSource.System)
        createdUser = User(
            id = creationEvent.subjectId,
            name = creationEvent.content.name,
            hashedPwd = creationEvent.content.hashedPwd
        )
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
