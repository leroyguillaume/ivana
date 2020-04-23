@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.api.security.Permission
import io.ivana.api.security.UserAlbumAuthorizationRepository
import io.ivana.core.*
import io.kotlintest.matchers.collections.shouldContainExactly
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
internal class AlbumEventRepositoryImplTest {
    private val pwdEncoder = BCryptPasswordEncoder()
    private val userCreationEventContent = UserEvent.Creation.Content(
        name = "admin",
        hashedPwd = pwdEncoder.encode("changeit"),
        role = Role.SuperAdmin
    )

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var userEventRepo: UserEventRepository

    @Autowired
    private lateinit var repo: AlbumEventRepositoryImpl

    @Autowired
    private lateinit var albumRepo: AlbumRepositoryImpl

    @Autowired
    private lateinit var authzRepo: UserAlbumAuthorizationRepository

    private lateinit var userCreationEvent: UserEvent.Creation
    private lateinit var createdUser: User

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        userCreationEvent = userEventRepo.saveCreationEvent(userCreationEventContent, EventSource.System)
        createdUser = User(
            id = userCreationEvent.subjectId,
            name = userCreationEvent.content.name,
            hashedPwd = userCreationEvent.content.hashedPwd,
            role = userCreationEvent.content.role,
            creationDate = userCreationEvent.date
        )
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
        fun `should return upload event with subject id and number`() {
            val expectedEvent = repo.saveCreationEvent(
                name = "album",
                source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1"))
            )
            val event = repo.fetch(expectedEvent.subjectId, expectedEvent.number)
            event shouldBe expectedEvent
        }
    }

    @Nested
    inner class saveCreationEvent {
        private lateinit var expectedEvent: AlbumEvent.Creation
        private lateinit var expectedAlbum: Album

        @BeforeEach
        fun beforeEach() {
            expectedEvent = UUID.randomUUID().let { id ->
                AlbumEvent.Creation(
                    date = OffsetDateTime.now(),
                    subjectId = id,
                    source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1")),
                    albumName = "album"
                )
            }
            expectedAlbum = Album(
                id = expectedEvent.subjectId,
                ownerId = createdUser.id,
                name = expectedEvent.albumName,
                creationDate = expectedEvent.date
            )
        }

        @Test
        fun `should return created event`() {
            val event = repo.saveCreationEvent(expectedEvent.albumName, expectedEvent.source)
            event shouldBe expectedEvent.copy(
                date = event.date,
                subjectId = event.subjectId
            )
            albumRepo.fetchById(event.subjectId) shouldBe expectedAlbum.copy(
                id = event.subjectId,
                creationDate = event.date
            )
            val permissions = authzRepo.fetch(createdUser.id, event.subjectId)
            permissions shouldContainExactly Permission.values().toSet()
        }
    }
}
