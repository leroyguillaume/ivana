@file:Suppress("ClassName")

package io.ivana.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ivana.api.config.AuthenticationProperties
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
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.net.InetAddress
import java.time.Clock
import java.time.Instant
import java.util.*

internal class AuthenticationServiceTest {
    private val props = AuthenticationProperties(
        secret = "changeit"
    )

    private lateinit var userEventRepo: UserEventRepository
    private lateinit var userRepo: UserRepository
    private lateinit var clock: Clock

    private lateinit var service: AuthenticationService

    @BeforeEach
    fun beforeEach() {
        userEventRepo = mockk()
        userRepo = mockk()
        clock = mockk()

        service = AuthenticationService(userEventRepo, userRepo, props, clock)
    }

    @Nested
    inner class authenticate {
        private val pwdEncoder = BCryptPasswordEncoder()
        private val password = "changeit"
        private val user = User(
            id = UUID.randomUUID(),
            name = "admin",
            hashedPwd = pwdEncoder.encode(password)
        )
        private val expectedJwt = Jwt(
            value = "jwt",
            expirationInSeconds = props.expirationInSeconds
        )
        private val loginEvent = UserEvent.Login(
            date = Instant.now(),
            subjectId = user.id,
            number = 1,
            source = EventSource.User(user.id, InetAddress.getByName("127.0.0.1"))
        )
        private val now = Instant.now()

        @Test
        fun `should throw exception if user does not exist`() {
            every { userRepo.fetchByName(user.name) } returns null
            val exception = assertThrows<BadCredentialsException> {
                service.authenticate(user.name, password, loginEvent.source.ip)
            }
            exception shouldHaveMessage "User '${user.name}' does not exist"
            verify { userRepo.fetchByName(user.name) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should throw exception if password is wrong`() {
            every { userRepo.fetchByName(user.name) } returns user
            val exception = assertThrows<BadCredentialsException> {
                service.authenticate(user.name, password.reversed(), loginEvent.source.ip)
            }
            exception shouldHaveMessage "Wrong password for user '${user.name}'"
            verify { userRepo.fetchByName(user.name) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should save login event and return generated jwt`() {
            every { userRepo.fetchByName(user.name) } returns user
            every { userEventRepo.saveLoginEvent(loginEvent.source) } returns loginEvent
            every { clock.instant() } returns now
            val jwt = service.authenticate(user.name, password, loginEvent.source.ip)
            val decodedJwt = JWT.require(Algorithm.HMAC512(props.secret))
                .build()
                .verify(jwt.value)
            decodedJwt.subject shouldBe user.name
            jwt shouldBe expectedJwt.copy(value = jwt.value)
            verify { userRepo.fetchByName(user.name) }
            verify { userEventRepo.saveLoginEvent(loginEvent.source) }
            verify { clock.instant() }
            confirmVerified(userRepo, userEventRepo, clock)
        }
    }

    @Nested
    inner class loadByUsername {
        private val user = User(
            id = UUID.randomUUID(),
            name = "admin",
            hashedPwd = "hashedPwd"
        )
        private val expectedPrincipal = UserPrincipal(user)

        @Test
        fun `should throw exception if user does not exist`() {
            every { userRepo.fetchByName(user.name) } returns null
            val exception = assertThrows<UsernameNotFoundException> { service.loadUserByUsername(user.name) }
            exception.shouldHaveMessage("User '${user.name}' does not exist")
            verify { userRepo.fetchByName(user.name) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should return principal`() {
            every { userRepo.fetchByName(user.name) } returns user
            val principal = service.loadUserByUsername(user.name)
            principal shouldBe expectedPrincipal
            verify { userRepo.fetchByName(user.name) }
            confirmVerified(userRepo)
        }
    }
}
