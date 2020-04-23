@file:Suppress("ClassName")

package io.ivana.api.security

import io.ivana.api.impl.cleanDb
import io.ivana.core.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.net.InetAddress
import java.util.*

@SpringBootTest
internal class UserAlbumAuthorizationRepositoryTest : AbstractAuthorizationRepositoryTest() {
    override val tableName = UserAlbumAuthorizationRepository.TableName
    override val subjectIdColumnName = UserAlbumAuthorizationRepository.UserIdColumnName
    override val resourceIdColumnName = UserAlbumAuthorizationRepository.AlbumIdColumnName

    private val pwdEncoder = BCryptPasswordEncoder()
    private val userCreationEventContent = UserEvent.Creation.Content(
        name = "admin",
        hashedPwd = pwdEncoder.encode("changeit"),
        role = Role.SuperAdmin
    )

    @Autowired
    private lateinit var userEventRepo: UserEventRepository

    @Autowired
    private lateinit var albumEventRepo: AlbumEventRepository

    @Autowired
    private lateinit var repo: UserAlbumAuthorizationRepository

    private lateinit var userCreationEvent: UserEvent.Creation
    private lateinit var albumCreationEvent: AlbumEvent.Creation

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        userCreationEvent = userEventRepo.saveCreationEvent(userCreationEventContent, EventSource.System)
        albumCreationEvent = albumEventRepo.saveCreationEvent(
            name = "album",
            source = EventSource.User(userCreationEvent.subjectId, InetAddress.getByName("127.0.0.1"))
        )
    }

    @Nested
    inner class fetch {
        @Test
        fun `should return empty set if user does not exist`() {
            val permissions = repo.fetch(UUID.randomUUID(), albumCreationEvent.subjectId)
            permissions.shouldBeEmpty()
        }

        @Test
        fun `should return empty set if album does not exist`() {
            val permissions = repo.fetch(userCreationEvent.subjectId, UUID.randomUUID())
            permissions.shouldBeEmpty()
        }

        @Test
        fun `should return empty set if user does not have rights`() {
            updateAuthorization(userCreationEvent.subjectId, albumCreationEvent.subjectId)
            val permissions = repo.fetch(userCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions.shouldBeEmpty()
        }

        @Test
        fun `should return read if user has read permission`() {
            updateAuthorization(userCreationEvent.subjectId, albumCreationEvent.subjectId, Permission.Read)
            val permissions = repo.fetch(userCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Read)
        }

        @Test
        fun `should return update if user has update permission`() {
            updateAuthorization(userCreationEvent.subjectId, albumCreationEvent.subjectId, Permission.Update)
            val permissions = repo.fetch(userCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Update)
        }

        @Test
        fun `should return delete if user has delete permission`() {
            updateAuthorization(userCreationEvent.subjectId, albumCreationEvent.subjectId, Permission.Delete)
            val permissions = repo.fetch(userCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Delete)
        }

        @Test
        fun `should return all if user has all permissions`() {
            val expectedPermissions = Permission.values()
            updateAuthorization(userCreationEvent.subjectId, albumCreationEvent.subjectId, *expectedPermissions)
            val permissions = repo.fetch(userCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions shouldContainExactly permissions.toSet()
        }
    }
}
