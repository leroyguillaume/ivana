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
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*

internal class UserServiceImplTest {
    private lateinit var userRepo: UserRepository
    private lateinit var userEventRepo: UserEventRepository
    private lateinit var service: UserServiceImpl

    @BeforeEach
    fun beforeEach() {
        userRepo = mockk()
        userEventRepo = mockk()

        service = UserServiceImpl(userRepo, userEventRepo)
    }

    @Nested
    inner class create {
        private val creationEvent = UserEvent.Creation(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.System,
            content = UserEvent.Creation.Content(
                name = "admin",
                hashedPwd = "changeit",
                role = Role.SuperAdmin
            )
        )
        private val expectedUser = User(
            id = creationEvent.subjectId,
            name = creationEvent.content.name,
            hashedPwd = creationEvent.content.hashedPwd,
            role = creationEvent.content.role,
            creationDate = creationEvent.date
        )

        @Test
        fun `should throw exception if username is already used`() {
            every { userRepo.fetchByName(expectedUser.name) } returns expectedUser
            val exception = assertThrows<UserAlreadyExistsException> {
                service.create(creationEvent.content, creationEvent.source)
            }
            exception.user shouldBe expectedUser
            verify { userRepo.fetchByName(expectedUser.name) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should save creation event`() {
            every { userRepo.fetchByName(expectedUser.name) } returns null
            every { userEventRepo.saveCreationEvent(creationEvent.content, creationEvent.source) } returns creationEvent
            every { userRepo.fetchById(expectedUser.id) } returns expectedUser
            val user = service.create(creationEvent.content, creationEvent.source)
            user shouldBe expectedUser
            verify { userRepo.fetchByName(expectedUser.name) }
            verify { userEventRepo.saveCreationEvent(creationEvent.content, creationEvent.source) }
            verify { userRepo.fetchById(expectedUser.id) }
            confirmVerified(userRepo, userEventRepo)
        }
    }

    @Nested
    inner class delete {
        private val event = UserEvent.Deletion(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1"))
        )

        @Test
        fun `should throw exception if user does not exist`() {
            every { userRepo.existsById(event.subjectId) } returns false
            val exception = assertThrows<EntityNotFoundException> { service.delete(event.subjectId, event.source) }
            exception shouldHaveMessage "User ${event.subjectId} does not exist"
            verify { userRepo.existsById(event.subjectId) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should delete user`() {
            every { userRepo.existsById(event.subjectId) } returns true
            every { userEventRepo.saveDeletionEvent(event.subjectId, event.source) } returns event
            service.delete(event.subjectId, event.source)
            verify { userRepo.existsById(event.subjectId) }
            verify { userEventRepo.saveDeletionEvent(event.subjectId, event.source) }
            confirmVerified(userRepo)
        }
    }

    @Nested
    inner class getAll {
        private val pageNo = 1
        private val pageSize = 3
        private val expectedPage = Page(
            content = listOf(
                User(
                    id = UUID.randomUUID(),
                    name = "user1",
                    hashedPwd = "hashedPwd",
                    role = Role.User,
                    creationDate = OffsetDateTime.now()
                ),
                User(
                    id = UUID.randomUUID(),
                    name = "user2",
                    hashedPwd = "hashedPwd",
                    role = Role.User,
                    creationDate = OffsetDateTime.now()
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )

        @Test
        fun `should return page`() {
            every { userRepo.fetchAll(pageNo - 1, pageSize) } returns expectedPage.content
            every { userRepo.count() } returns 2
            val page = service.getAll(pageNo, pageSize)
            page shouldBe expectedPage
            verify { userRepo.fetchAll(pageNo - 1, pageSize) }
            verify { userRepo.count() }
            confirmVerified(userRepo)
        }
    }

    @Nested
    inner class getAllByIds {
        private val expectedUsers = setOf(
            User(
                id = UUID.randomUUID(),
                name = "user1",
                hashedPwd = "hashedPwd",
                role = Role.User,
                creationDate = OffsetDateTime.now()
            ),
            User(
                id = UUID.randomUUID(),
                name = "user2",
                hashedPwd = "hashedPwd",
                role = Role.User,
                creationDate = OffsetDateTime.now()
            )
        )
        private val ids = expectedUsers.map { it.id }.toSet()

        @Test
        fun `should throw exception if entities not found`() {
            every { userRepo.fetchAllByIds(ids) } returns emptySet()
            val exception = assertThrows<ResourcesNotFoundException> { service.getAllByIds(ids) }
            exception.ids shouldBe ids
            verify { userRepo.fetchAllByIds(ids) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should return all users`() {
            every { userRepo.fetchAllByIds(ids) } returns expectedUsers
            val users = service.getAllByIds(ids)
            users shouldBe expectedUsers
            verify { userRepo.fetchAllByIds(ids) }
            confirmVerified(userRepo)
        }
    }

    @Nested
    inner class getByName {
        private val expectedUser = User(
            id = UUID.randomUUID(),
            name = "admin",
            hashedPwd = "changeit",
            role = Role.SuperAdmin,
            creationDate = OffsetDateTime.now()
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
            role = Role.SuperAdmin,
            creationDate = OffsetDateTime.now()
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
    inner class suggest {
        private val expectedUsers = listOf(
            User(
                id = UUID.randomUUID(),
                name = "user1",
                hashedPwd = "hashedPwd",
                role = Role.User,
                creationDate = OffsetDateTime.now()
            ),
            User(
                id = UUID.randomUUID(),
                name = "user2",
                hashedPwd = "hashedPwd",
                role = Role.User,
                creationDate = OffsetDateTime.now()
            )
        )
        private val name = "user"
        private val count = 2

        @Test
        fun `should return suggested users`() {
            every { userRepo.suggest(name, count) } returns expectedUsers
            val users = service.suggest(name, count)
            users shouldBe expectedUsers
            verify { userRepo.suggest(name, count) }
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

        @Test
        fun `should throw exception if user does not exist`() {
            every { userRepo.existsById(event.subjectId) } returns false
            val exception = assertThrows<EntityNotFoundException> {
                service.updatePassword(event.subjectId, event.newHashedPwd, event.source)
            }
            exception shouldHaveMessage "User ${event.subjectId} does not exist"
            verify { userRepo.existsById(event.subjectId) }
            confirmVerified(userRepo)
        }

        @Test
        fun `should update password`() {
            every { userRepo.existsById(event.subjectId) } returns true
            every {
                userEventRepo.savePasswordUpdateEvent(event.subjectId, event.newHashedPwd, event.source)
            } returns event
            service.updatePassword(event.subjectId, event.newHashedPwd, event.source)
            verify { userRepo.existsById(event.subjectId) }
            confirmVerified(userRepo)
        }
    }
}
