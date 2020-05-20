@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.net.InetAddress
import java.util.*

@SpringBootTest
internal class UserAlbumAuthorizationRepositoryImplTest : AbstractAuthorizationRepositoryTest() {
    private val pwdEncoder = BCryptPasswordEncoder()
    private val ownerCreationEventContent = UserEvent.Creation.Content(
        name = "admin",
        hashedPwd = pwdEncoder.encode("changeit"),
        role = Role.SuperAdmin
    )

    @Autowired
    private lateinit var userEventRepo: UserEventRepository

    @Autowired
    private lateinit var albumEventRepo: AlbumEventRepository

    @Autowired
    private lateinit var repo: UserAlbumAuthorizationRepositoryImpl

    private lateinit var ownerCreationEvent: UserEvent.Creation
    private lateinit var albumCreationEvent: AlbumEvent.Creation

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        ownerCreationEvent = userEventRepo.saveCreationEvent(ownerCreationEventContent, EventSource.System)
        albumCreationEvent = albumEventRepo.saveCreationEvent(
            name = "album",
            source = EventSource.User(ownerCreationEvent.subjectId, InetAddress.getByName("127.0.0.1"))
        )
    }

    @Nested
    inner class count {
        private val user1CreationEventContent = UserEvent.Creation.Content(
            name = "user1",
            hashedPwd = pwdEncoder.encode("changeit"),
            role = Role.SuperAdmin
        )
        private val user2CreationEventContent = user1CreationEventContent.copy(name = "user2")

        private lateinit var user1CreationEvent: UserEvent.Creation
        private lateinit var user2CreationEvent: UserEvent.Creation
        private lateinit var subjsPerms: List<SubjectPermissions>

        @BeforeEach
        fun beforeEach() {
            user1CreationEvent = userEventRepo.saveCreationEvent(user1CreationEventContent, EventSource.System)
            user2CreationEvent = userEventRepo.saveCreationEvent(user2CreationEventContent, EventSource.System)
            subjsPerms = listOf(
                SubjectPermissions(
                    subjectId = ownerCreationEvent.subjectId,
                    permissions = setOf(Permission.Read, Permission.Update, Permission.Delete)
                ),
                SubjectPermissions(
                    subjectId = user1CreationEvent.subjectId,
                    permissions = setOf(Permission.Read)
                ),
                SubjectPermissions(
                    subjectId = user2CreationEvent.subjectId,
                    permissions = setOf(Permission.Read, Permission.Update)
                )
            )
            subjsPerms.forEach { subjPerms ->
                updateUserAlbumAuthorizations(
                    subjectId = subjPerms.subjectId,
                    resourceId = albumCreationEvent.subjectId,
                    permissions = *subjPerms.permissions.toTypedArray()
                )
            }
        }

        @Test
        fun `should return count of permissions about album`() {
            repo.count(albumCreationEvent.subjectId) shouldBe subjsPerms.size
        }
    }

    @Nested
    inner class fetch {
        @Test
        fun `should return null if user does not exist`() {
            val permissions = repo.fetch(UUID.randomUUID(), albumCreationEvent.subjectId)
            permissions.shouldBeNull()
        }

        @Test
        fun `should return null if album does not exist`() {
            val permissions = repo.fetch(ownerCreationEvent.subjectId, UUID.randomUUID())
            permissions.shouldBeNull()
        }

        @Test
        fun `should return null if no authorization defined`() {
            deleteUserAlbumAuthorizations(ownerCreationEvent.subjectId, albumCreationEvent.subjectId)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions.shouldBeNull()
        }

        @Test
        fun `should return empty set if user does not have rights`() {
            updateUserAlbumAuthorizations(ownerCreationEvent.subjectId, albumCreationEvent.subjectId)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions!!.shouldBeEmpty()
        }

        @Test
        fun `should return read if user has read permission`() {
            updateUserAlbumAuthorizations(ownerCreationEvent.subjectId, albumCreationEvent.subjectId, Permission.Read)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Read)
        }

        @Test
        fun `should return update if user has update permission`() {
            updateUserAlbumAuthorizations(ownerCreationEvent.subjectId, albumCreationEvent.subjectId, Permission.Update)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Update)
        }

        @Test
        fun `should return delete if user has delete permission`() {
            updateUserAlbumAuthorizations(ownerCreationEvent.subjectId, albumCreationEvent.subjectId, Permission.Delete)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Delete)
        }

        @Test
        fun `should return all if user has all permissions`() {
            val expectedPermissions = Permission.values()
            updateUserAlbumAuthorizations(
                subjectId = ownerCreationEvent.subjectId,
                resourceId = albumCreationEvent.subjectId,
                permissions = *expectedPermissions
            )
            val permissions = repo.fetch(ownerCreationEvent.subjectId, albumCreationEvent.subjectId)
            permissions shouldContainExactly permissions!!.toSet()
        }
    }

    @Nested
    inner class fetchAll {
        private val user1CreationEventContent = UserEvent.Creation.Content(
            name = "user1",
            hashedPwd = pwdEncoder.encode("changeit"),
            role = Role.SuperAdmin
        )
        private val user2CreationEventContent = user1CreationEventContent.copy(name = "user2")

        private lateinit var user1CreationEvent: UserEvent.Creation
        private lateinit var user2CreationEvent: UserEvent.Creation
        private lateinit var subjsPerms: List<SubjectPermissions>

        @BeforeEach
        fun beforeEach() {
            user1CreationEvent = userEventRepo.saveCreationEvent(user1CreationEventContent, EventSource.System)
            user2CreationEvent = userEventRepo.saveCreationEvent(user2CreationEventContent, EventSource.System)
            subjsPerms = listOf(
                SubjectPermissions(
                    subjectId = ownerCreationEvent.subjectId,
                    permissions = setOf(Permission.Read, Permission.Update, Permission.Delete)
                ),
                SubjectPermissions(
                    subjectId = user1CreationEvent.subjectId,
                    permissions = setOf(Permission.Read)
                ),
                SubjectPermissions(
                    subjectId = user2CreationEvent.subjectId,
                    permissions = setOf(Permission.Read, Permission.Update)
                )
            )
            subjsPerms.forEach { subjPerms ->
                updateUserAlbumAuthorizations(
                    subjectId = subjPerms.subjectId,
                    resourceId = albumCreationEvent.subjectId,
                    permissions = *subjPerms.permissions.toTypedArray()
                )
            }
        }

        @Test
        fun `should return all permissions about album`() {
            repo.fetchAll(albumCreationEvent.subjectId, 1, 10) shouldBe subjsPerms
                .sortedBy { it.subjectId.toString() }
                .subList(1, subjsPerms.size)
        }
    }
}
