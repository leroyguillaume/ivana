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
internal class UserPhotoAuthorizationRepositoryTest : AbstractAuthorizationRepositoryTest() {
    override val tableName = UserPhotoAuthorizationRepository.TableName
    override val subjectIdColumnName = UserPhotoAuthorizationRepository.UserIdColumnName
    override val resourceIdColumnName = UserPhotoAuthorizationRepository.PhotoIdColumnName

    private val pwdEncoder = BCryptPasswordEncoder()
    private val userCreationEventContent = UserEvent.Creation.Content(
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
    private lateinit var repo: UserPhotoAuthorizationRepository

    private lateinit var userCreationEvent: UserEvent.Creation
    private lateinit var photoUploadEvent: PhotoEvent.Upload

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        userCreationEvent = userEventRepo.saveCreationEvent(userCreationEventContent, EventSource.System)
        photoUploadEvent = photoEventRepo.saveUploadEvent(
            content = photoUploadEventContent,
            source = EventSource.User(userCreationEvent.subjectId, InetAddress.getByName("127.0.0.1"))
        )
    }

    @Nested
    inner class fetch {
        @Test
        fun `should return empty set if user does not exist`() {
            val permissions = repo.fetch(UUID.randomUUID(), photoUploadEvent.subjectId)
            permissions.shouldBeEmpty()
        }

        @Test
        fun `should return empty set if photo does not exist`() {
            val permissions = repo.fetch(userCreationEvent.subjectId, UUID.randomUUID())
            permissions.shouldBeEmpty()
        }

        @Test
        fun `should return empty set if user does not have rights`() {
            updateAuthorization(userCreationEvent.subjectId, photoUploadEvent.subjectId)
            val permissions = repo.fetch(userCreationEvent.subjectId, photoUploadEvent.subjectId)
            permissions.shouldBeEmpty()
        }

        @Test
        fun `should return read if user has read permission`() {
            updateAuthorization(userCreationEvent.subjectId, photoUploadEvent.subjectId, Permission.Read)
            val permissions = repo.fetch(userCreationEvent.subjectId, photoUploadEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Read)
        }

        @Test
        fun `should return update if user has update permission`() {
            updateAuthorization(userCreationEvent.subjectId, photoUploadEvent.subjectId, Permission.Update)
            val permissions = repo.fetch(userCreationEvent.subjectId, photoUploadEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Update)
        }

        @Test
        fun `should return delete if user has delete permission`() {
            updateAuthorization(userCreationEvent.subjectId, photoUploadEvent.subjectId, Permission.Delete)
            val permissions = repo.fetch(userCreationEvent.subjectId, photoUploadEvent.subjectId)
            permissions shouldContainExactly setOf(Permission.Delete)
        }

        @Test
        fun `should return all if user has all permissions`() {
            val expectedPermissions = Permission.values()
            updateAuthorization(userCreationEvent.subjectId, photoUploadEvent.subjectId, *expectedPermissions)
            val permissions = repo.fetch(userCreationEvent.subjectId, photoUploadEvent.subjectId)
            permissions shouldContainExactly permissions.toSet()
        }
    }
}
