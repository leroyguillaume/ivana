@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
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
import java.util.*

@SpringBootTest
internal class PhotoRepositoryImplTest {
    private val pwdEncoder = BCryptPasswordEncoder()
    private val userCreationEventContent = UserEvent.Creation.Content(
        name = "admin",
        hashedPwd = pwdEncoder.encode("changeit")
    )
    private val photoUploadEventContent = PhotoEvent.Upload.Content(
        type = Photo.Type.Jpg,
        hash = "hash"
    )

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    private lateinit var repo: PhotoRepositoryImpl
    private lateinit var eventRepo: PhotoEventRepository
    private lateinit var userEventRepo: UserEventRepository
    private lateinit var userCreationEvent: UserEvent.Creation
    private lateinit var createdUser: User
    private lateinit var uploadEvent: PhotoEvent.Upload
    private lateinit var uploadedPhoto: Photo

    @BeforeEach
    fun beforeEach() {
        userEventRepo = UserEventRepositoryImpl(jdbc)
        eventRepo = PhotoEventRepositoryImpl(jdbc)
        repo = PhotoRepositoryImpl(jdbc)

        cleanDb(jdbc)
        userCreationEvent = userEventRepo.saveCreationEvent(userCreationEventContent, EventSource.System)
        createdUser = User(
            id = userCreationEvent.subjectId,
            name = userCreationEvent.content.name,
            hashedPwd = userCreationEvent.content.hashedPwd
        )
        uploadEvent = eventRepo.saveUploadEvent(
            content = photoUploadEventContent,
            source = EventSource.User(createdUser.id, InetAddress.getByName("127.0.0.1"))
        )
        uploadedPhoto = Photo(
            id = uploadEvent.subjectId,
            ownerId = createdUser.id,
            uploadDate = uploadEvent.date,
            type = uploadEvent.content.type,
            hash = uploadEvent.content.hash,
            no = 1
        )
    }

    @Nested
    inner class fetchById {
        @Test
        fun `should return null if photo does not exist`() {
            val photo = repo.fetchById(UUID.randomUUID())
            photo.shouldBeNull()
        }

        @Test
        fun `should return photo with id`() {
            val photo = repo.fetchById(uploadedPhoto.id)
            photo shouldBe uploadedPhoto
        }
    }

    @Nested
    inner class fetchByHash {
        @Test
        fun `should return null if photo does not exist`() {
            val photo = repo.fetchByHash(uploadedPhoto.ownerId, uploadedPhoto.hash.reversed())
            photo.shouldBeNull()
        }

        @Test
        fun `should return null if owner is not the same`() {
            val photo = repo.fetchByHash(UUID.randomUUID(), uploadedPhoto.hash)
            photo.shouldBeNull()
        }

        @Test
        fun `should return photo with hash`() {
            val photo = repo.fetchByHash(uploadedPhoto.ownerId, uploadedPhoto.hash)
            photo shouldBe uploadedPhoto
        }
    }
}
