@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.EventSource
import io.ivana.core.User
import io.ivana.core.UserEvent
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

internal class UserRepositoryImplTest {
    private val jdbc = jdbcTemplate()
    private val eventRepo = UserEventRepositoryImpl(jdbc)
    private val pwdEncoder = BCryptPasswordEncoder()
    private val creationEventContent = UserEvent.Creation.Content(
        name = "admin",
        hashedPwd = pwdEncoder.encode("foo123")
    )
    private val creationEventSource = EventSource.System

    private lateinit var repo: UserRepositoryImpl
    private lateinit var creationEvent: UserEvent.Creation
    private lateinit var createdUser: User

    @BeforeEach
    fun beforeEach() {
        repo = UserRepositoryImpl(jdbc)

        cleanDb(jdbc)
        creationEvent = eventRepo.saveCreationEvent(creationEventContent, creationEventSource)
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
