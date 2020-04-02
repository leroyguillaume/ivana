@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.EventSource
import io.ivana.core.Role
import io.ivana.core.UserEvent
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
internal class UserEventRepositoryImplTest {
    private val pwdEncoder = BCryptPasswordEncoder()

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var repo: UserEventRepositoryImpl

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
    }

    @Nested
    inner class fetch {
        private val subjectId = UUID.randomUUID()
        private val number = 1L

        @Test
        fun `should return null if event does not exist`() {
            val event = repo.fetch(subjectId, number)
            event.shouldBeNull()
        }

        @Test
        fun `should return creation event with subject id and number`() {
            val expectedEvent = repo.saveCreationEvent(
                content = UserEvent.Creation.Content(
                    name = "admin",
                    hashedPwd = pwdEncoder.encode("changeit"),
                    role = Role.SuperAdmin
                ),
                source = EventSource.System
            )
            val event = repo.fetch(expectedEvent.subjectId, expectedEvent.number)
            event shouldBe expectedEvent
        }

        @Test
        fun `should return login event with subject id and number`() {
            val expectedEvent = repo.saveLoginEvent(EventSource.User(subjectId, InetAddress.getByName("127.0.0.1")))
            val event = repo.fetch(expectedEvent.subjectId, expectedEvent.number)
            event shouldBe expectedEvent
        }
    }

    @Nested
    inner class saveCreationEvent {
        private val expectedEvent = UUID.randomUUID().let { id ->
            UserEvent.Creation(
                date = OffsetDateTime.now(),
                subjectId = id,
                source = EventSource.System,
                content = UserEvent.Creation.Content(
                    name = "admin",
                    hashedPwd = pwdEncoder.encode("changeit"),
                    role = Role.SuperAdmin
                )
            )
        }

        @Test
        fun `should return created event`() {
            val event = repo.saveCreationEvent(expectedEvent.content, expectedEvent.source)
            event shouldBe expectedEvent.copy(
                date = event.date,
                subjectId = event.subjectId
            )
        }
    }

    @Nested
    inner class saveLoginEvent {
        private val expectedEvent = UUID.randomUUID().let { id ->
            UserEvent.Login(
                date = OffsetDateTime.now(),
                subjectId = id,
                number = 1,
                source = EventSource.User(id, InetAddress.getByName("127.0.0.1"))
            )
        }

        @Test
        fun `should return created event`() {
            val event = repo.saveLoginEvent(expectedEvent.source)
            event shouldBe expectedEvent.copy(date = event.date)
        }
    }
}
