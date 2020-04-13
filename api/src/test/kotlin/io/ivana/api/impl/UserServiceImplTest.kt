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
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.OffsetDateTime
import java.util.*

internal class UserServiceImplTest {
    private lateinit var userRepo: UserRepository
    private lateinit var userEventRepo: UserEventRepository
    private lateinit var pwdEncoder: PasswordEncoder
    private lateinit var service: UserServiceImpl

    @BeforeEach
    fun beforeEach() {
        userRepo = mockk()
        userEventRepo = mockk()
        pwdEncoder = mockk()

        service = UserServiceImpl(userRepo, userEventRepo, pwdEncoder)
    }

    @Nested
    inner class findByName {
        private val expectedUser = User(
            id = UUID.randomUUID(),
            name = "admin",
            hashedPwd = "changeit",
            role = Role.SuperAdmin
        )

        @Test
        fun `should throw exception if user does not exist`() {
            every { userRepo.fetchByName(expectedUser.name) } returns null
            val exception = assertThrows<EntityNotFoundException> { service.getByName(expectedUser.name) }
            exception shouldHaveMessage "User '${expectedUser.name}' does not exist"
            verify { userRepo.fetchByName(expectedUser.name) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should return user with name`() {
            every { userRepo.fetchByName(expectedUser.name) } returns expectedUser
            val user = service.getByName(expectedUser.name)
            user shouldBe expectedUser
            verify { userRepo.fetchByName(expectedUser.name) }
            confirmVerified(userRepo)
        }
    }

    @Nested
    inner class getById {
        private val expectedUser = User(
            id = UUID.randomUUID(),
            name = "admin",
            hashedPwd = "hashedPwd",
            role = Role.SuperAdmin
        )

        @Test
        fun `should throw exception if user does not exist`() {
            every { userRepo.fetchById(expectedUser.id) } returns null
            val exception = assertThrows<EntityNotFoundException> { service.getById(expectedUser.id) }
            exception shouldHaveMessage "User ${expectedUser.id} does not exist"
            verify { userRepo.fetchById(expectedUser.id) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should return user with id`() {
            every { userRepo.fetchById(expectedUser.id) } returns expectedUser
            val user = service.getById(expectedUser.id)
            user shouldBe expectedUser
            verify { userRepo.fetchById(user.id) }
            confirmVerified(userRepo)
        }
    }

    @Nested
    inner class updatePassword {
        private val event = UserEvent.PasswordUpdate(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.System,
            newHashedPwd = "newHashedPwd"
        )
        private val newPwd = "newPwd"

        @Test
        fun `should throw exception if user does not exist`() {
            every { userRepo.existsById(event.subjectId) } returns false
            val exception = assertThrows<EntityNotFoundException> {
                service.updatePassword(event.subjectId, newPwd, event.source)
            }
            exception shouldHaveMessage "User ${event.subjectId} does not exist"
            verify { userRepo.existsById(event.subjectId) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should update password`() {
            every { userRepo.existsById(event.subjectId) } returns true
            every { pwdEncoder.encode(newPwd) } returns event.newHashedPwd
            every {
                userEventRepo.savePasswordUpdateEvent(event.subjectId, event.newHashedPwd, event.source)
            } returns event
            service.updatePassword(event.subjectId, newPwd, event.source)
            verify { userRepo.existsById(event.subjectId) }
            verify { pwdEncoder.encode(newPwd) }
            confirmVerified(userRepo, pwdEncoder)
        }
    }
}
