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
internal class UserPhotoAuthorizationRepositoryImplTest : AbstractAuthorizationRepositoryTest() {
    override val tableName = UserPhotoAuthorizationRepositoryImpl.TableName
    override val subjectIdColumnName = UserPhotoAuthorizationRepositoryImpl.UserIdColumnName
    override val resourceIdColumnName = UserPhotoAuthorizationRepositoryImpl.PhotoIdColumnName

    private val pwdEncoder = BCryptPasswordEncoder()
    private val ownerCreationEventContent = UserEvent.Creation.Content(
        name = "admin",
        hashedPwd = pwdEncoder.encode("changeit"),
        role = Role.SuperAdmin
    )
    private val photoUploadEventContent = PhotoEvent.Upload.Content(
        type = Photo.Type.Jpg,
        hash = "hash"
    )

    @Autowired
    private lateinit var userEventRepo: UserEventRepository

    @Autowired
    private lateinit var photoEventRepo: PhotoEventRepository

    @Autowired
    private lateinit var repo: UserPhotoAuthorizationRepositoryImpl

    private lateinit var ownerCreationEvent: UserEvent.Creation
    private lateinit var photoUploadEvent: PhotoEvent.Upload

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        ownerCreationEvent = userEventRepo.saveCreationEvent(ownerCreationEventContent, EventSource.System)
        photoUploadEvent = photoEventRepo.saveUploadEvent(
            content = photoUploadEventContent,
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
                updateAuthorization(
                    subjectId = subjPerms.subjectId,
                    resourceId = photoUploadEvent.subjectId,
                    permissions = *subjPerms.permissions.toTypedArray()
                )
            }
        }

        @Test
        fun `should return count of permissions about photo`() {
            repo.count(photoUploadEvent.subjectId) shouldBe subjsPerms.size
        }
    }

    @Nested
    inner class fetch {
        @Test
        fun `should return null if user does not exist`() {
            val permissions = repo.fetch(UUID.randomUUID(), photoUploadEvent.subjectId)
            permissions.shouldBeNull()
        }

        @Test
        fun `should return null if photo does not exist`() {
            val permissions = repo.fetch(ownerCreationEvent.subjectId, UUID.randomUUID())
            permissions.shouldBeNull()
        }

        @Test
        fun `should return null if no authorization defined`() {
            deleteAuthorizations(ownerCreationEvent.subjectId, photoUploadEvent.subjectId)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, photoUploadEvent.subjectId)
            permissions.shouldBeNull()
        }

        @Test
        fun `should return empty set if user does not have rights`() {
            updateAuthorization(ownerCreationEvent.subjectId, photoUploadEvent.subjectId)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, photoUploadEvent.subjectId)
            permissions!!.shouldBeEmpty()
        }

        @Test
        fun `should return read if user has read permission`() {
            updateAuthorization(ownerCreationEvent.subjectId, photoUploadEvent.subjectId, Permission.Read)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, photoUploadEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Read)
        }

        @Test
        fun `should return update if user has update permission`() {
            updateAuthorization(ownerCreationEvent.subjectId, photoUploadEvent.subjectId, Permission.Update)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, photoUploadEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Update)
        }

        @Test
        fun `should return delete if user has delete permission`() {
            updateAuthorization(ownerCreationEvent.subjectId, photoUploadEvent.subjectId, Permission.Delete)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, photoUploadEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Delete)
        }

        @Test
        fun `should return all if user has all permissions`() {
            val expectedPermissions = Permission.values()
            updateAuthorization(ownerCreationEvent.subjectId, photoUploadEvent.subjectId, *expectedPermissions)
            val permissions = repo.fetch(ownerCreationEvent.subjectId, photoUploadEvent.subjectId)
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
                updateAuthorization(
                    subjectId = subjPerms.subjectId,
                    resourceId = photoUploadEvent.subjectId,
                    permissions = *subjPerms.permissions.toTypedArray()
                )
            }
        }

        @Test
        fun `should return all permissions about photos`() {
            repo.fetchAll(photoUploadEvent.subjectId, 1, 10) shouldBe subjsPerms
                .sortedBy { it.subjectId.toString() }
                .subList(1, subjsPerms.size)
        }
    }
}
