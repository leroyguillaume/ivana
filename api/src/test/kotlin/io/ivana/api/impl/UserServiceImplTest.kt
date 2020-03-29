@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.EntityNotFoundException
import io.ivana.core.Role
import io.ivana.core.User
import io.ivana.core.UserRepository
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
import java.util.*

internal class UserServiceImplTest {
    private lateinit var userRepo: UserRepository
    private lateinit var service: UserServiceImpl

    @BeforeEach
    fun beforeEach() {
        userRepo = mockk()

        service = UserServiceImpl(userRepo)
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
            val exception = assertThrows<EntityNotFoundException> { service.findByName(expectedUser.name) }
            exception shouldHaveMessage "User '${expectedUser.name}' does not exist"
            verify { userRepo.fetchByName(expectedUser.name) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should return user with name`() {
            every { userRepo.fetchByName(expectedUser.name) } returns expectedUser
            val user = service.findByName(expectedUser.name)
            user shouldBe expectedUser
            verify { userRepo.fetchByName(expectedUser.name) }
            confirmVerified(userRepo)
        }
    }
}
