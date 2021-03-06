@file:Suppress("ClassName")

package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ivana.api.impl.UserAlreadyExistsException
import io.ivana.api.web.AbstractControllerTest
import io.ivana.core.Page
import io.ivana.core.Role
import io.ivana.core.User
import io.ivana.core.UserEvent
import io.ivana.dto.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
internal class UserControllerTest : AbstractControllerTest() {
    @Nested
    inner class create {
        private val method = HttpMethod.POST
        private val uri = UserApiEndpoint
        private val creationDto = UserCreationDto(
            name = "user",
            pwd = "changeit",
            role = RoleDto.User
        )
        private val eventContent = UserEvent.Creation.Content(
            name = creationDto.name,
            hashedPwd = "hashedPwd",
            role = Role.User
        )
        private val user = User(
            id = UUID.randomUUID(),
            name = creationDto.name,
            hashedPwd = eventContent.hashedPwd,
            role = eventContent.role,
            creationDate = OffsetDateTime.now()
        )
        private val userDto = user.toDto()

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 400 if params are too short`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(
                    creationDto.copy(
                        name = "a",
                        pwd = "change"
                    )
                ),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(
                        sizeErrorDto("name", UserNameMinSize, UserNameMaxSize),
                        sizeErrorDto("pwd", UserPasswordMinSize)
                    )
                )
            )
        }

        @Test
        fun `should return 400 if name is too long`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(
                    creationDto.copy(name = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                ),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(sizeErrorDto("name", UserNameMinSize, UserNameMaxSize))
                )
            )
        }

        @Test
        fun `should return 409 if user already exists`() = authenticated(adminPrincipal) {
            whenever(pwdEncoder.encode(creationDto.pwd)).thenReturn(user.hashedPwd)
            whenever(userService.create(eventContent, source)).thenAnswer {
                throw UserAlreadyExistsException(user)
            }
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(creationDto),
                status = HttpStatus.CONFLICT,
                respDto = ErrorDto.DuplicateResource(URI("$UserApiEndpoint/${user.id}"))
            )
            verify(pwdEncoder).encode(creationDto.pwd)
            verify(userService).create(eventContent, source)
        }

        @Test
        fun `should return 403 if user is not admin or super admin`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(creationDto),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
        }

        @Test
        fun `should return 201 (admin)`() = authenticated(adminPrincipal) {
            whenever(pwdEncoder.encode(creationDto.pwd)).thenReturn(user.hashedPwd)
            whenever(userService.create(eventContent, source)).thenReturn(user)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(creationDto),
                status = HttpStatus.CREATED,
                respDto = userDto
            )
            verify(pwdEncoder).encode(creationDto.pwd)
            verify(userService).create(eventContent, source)
        }

        @Test
        fun `should return 201 (super_admin)`() = authenticated(superAdminPrincipal) {
            whenever(pwdEncoder.encode(creationDto.pwd)).thenReturn(user.hashedPwd)
            whenever(userService.create(eventContent, source)).thenReturn(user)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(creationDto),
                status = HttpStatus.CREATED,
                respDto = userDto
            )
            verify(pwdEncoder).encode(creationDto.pwd)
            verify(userService).create(eventContent, source)
        }
    }

    @Nested
    inner class delete {
        private val userId = UUID.randomUUID()
        private val method = HttpMethod.DELETE
        private val uri = "$UserApiEndpoint/$userId"

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 403 if user does not exist`() = authenticated {
            whenever(userRepo.fetchById(userId)).thenReturn(null)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userRepo).fetchById(userId)
        }

        @Test
        fun `should return 403 if user tries to delete user`() = authenticated {
            whenever(userRepo.fetchById(userId)).thenReturn(userPrincipal.user) // Trick here: userId != userPrincipal.user.id
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userRepo).fetchById(userId)
        }

        @Test
        fun `should return 403 if user tries to delete admin`() = authenticated {
            whenever(userRepo.fetchById(userId)).thenReturn(adminPrincipal.user)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userRepo).fetchById(userId)
        }

        @Test
        fun `should return 403 if user tries to delete super admin`() = authenticated {
            whenever(userRepo.fetchById(userId)).thenReturn(superAdminPrincipal.user)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userRepo).fetchById(userId)
        }

        @Test
        fun `should return 403 if admin tries to delete admin`() = authenticated(adminPrincipal) {
            whenever(userRepo.fetchById(userId)).thenReturn(adminPrincipal.user)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userRepo).fetchById(userId)
        }

        @Test
        fun `should return 403 if admin tries to delete super admin`() = authenticated(adminPrincipal) {
            whenever(userRepo.fetchById(userId)).thenReturn(superAdminPrincipal.user)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userRepo).fetchById(userId)
        }

        @Test
        fun `should return 403 if admin tries to delete itself`() = authenticated(adminPrincipal) {
            callAndExpectDto(
                method = method,
                uri = "$UserApiEndpoint/${principal.user.id}",
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
        }

        @Test
        fun `should return 403 if super admin tries to delete itself`() = authenticated(superAdminPrincipal) {
            callAndExpectDto(
                method = method,
                uri = "$UserApiEndpoint/${principal.user.id}",
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
        }

        @Test
        fun `should return 204 if admin deleted user`() = authenticated(adminPrincipal) {
            whenever(userRepo.fetchById(userId)).thenReturn(userPrincipal.user)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.NO_CONTENT
            )
            verify(userRepo).fetchById(userId)
            verify(userService).delete(userId, source)
        }

        @Test
        fun `should return 204 if super admin deleted user`() = authenticated(superAdminPrincipal) {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.NO_CONTENT
            )
            verify(userRepo, never()).fetchById(userId)
            verify(userService).delete(userId, source)
        }

        @Test
        fun `should return 204 if super admin deleted admin`() = authenticated(superAdminPrincipal) {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.NO_CONTENT
            )
            verify(userRepo, never()).fetchById(userId)
            verify(userService).delete(userId, source)
        }

        @Test
        fun `should return 204 if super admin deleted super admin`() = authenticated(superAdminPrincipal) {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.NO_CONTENT
            )
            verify(userRepo, never()).fetchById(userId)
            verify(userService).delete(userId, source)
        }
    }

    @Nested
    inner class getAll {
        private val pageNo = 2
        private val pageSize = 3
        private val page = Page(
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
        private val pageDto = page.toDto { it.toDto() }
        private val method = HttpMethod.GET
        private val uri = UserApiEndpoint

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 400 if parameters are lower than 1`() = authenticated(adminPrincipal) {
            callAndExpectDto(
                method = method,
                params = mapOf(
                    PageParamName to listOf("-1"),
                    SizeParamName to listOf("-1")
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(minErrorDto(PageParamName, 1), minErrorDto(SizeParamName, 1))
                )
            )
        }

        @Test
        fun `should return 403 if user is not admin or super admin`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
        }

        @Test
        fun `should return 200 (admin)`() = authenticated(adminPrincipal) {
            whenever(userService.getAll(pageNo, pageSize)).thenReturn(page)
            callAndExpectDto(
                method = method,
                params = mapOf(
                    PageParamName to listOf(pageNo.toString()),
                    SizeParamName to listOf(pageSize.toString())
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = pageDto
            )
            verify(userService).getAll(pageNo, pageSize)
        }

        @Test
        fun `should return 200 (super admin)`() = authenticated(superAdminPrincipal) {
            whenever(userService.getAll(pageNo, pageSize)).thenReturn(page)
            callAndExpectDto(
                method = method,
                params = mapOf(
                    PageParamName to listOf(pageNo.toString()),
                    SizeParamName to listOf(pageSize.toString())
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = pageDto
            )
            verify(userService).getAll(pageNo, pageSize)
        }
    }

    @Nested
    inner class me {
        private val method = HttpMethod.GET
        private val uri = "$UserApiEndpoint$MeEndpoint"
        private val dto = userPrincipal.user.toDto()

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 200`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = dto
            )
        }
    }

    @Nested
    inner class suggest {
        private val users = listOf(
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
        private val q = " user "
        private val count = 10
        private val dto = users.map { it.toDto() }
        private val method = HttpMethod.GET
        private val uri = "$UserApiEndpoint$SuggestEndpoint"

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 400 if parameters are blank`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                params = mapOf(
                    QParamName to listOf(" ")
                ),
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(blankErrorDto(QParamName))
                )
            )
        }

        @Test
        fun `should return 400 if parameters are lower than 1`() = authenticated {
            callAndExpectDto(
                method = method,
                params = mapOf(
                    QParamName to listOf(q),
                    CountParamName to listOf("0")
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(minErrorDto(CountParamName, 1))
                )
            )
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(userService.suggest(q.trim(), count)).thenReturn(users)
            callAndExpectDto(
                method = method,
                params = mapOf(
                    QParamName to listOf(q),
                    CountParamName to listOf(count.toString())
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = dto
            )
            verify(userService).suggest(q.trim(), count)
        }
    }

    @Nested
    inner class updatePassword {
        private val method = HttpMethod.PUT
        private val uri = "$UserApiEndpoint$PasswordUpdateEndpoint"
        private val dto = PasswordUpdateDto("changeit")
        private val hashedPwd = "hashedPwd"

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 400 if password is too short`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(dto.copy("change")),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(sizeErrorDto("newPwd", UserPasswordMinSize))
                )
            )
        }

        @Test
        fun `should return 204`() = authenticated {
            whenever(pwdEncoder.encode(dto.newPwd)).thenReturn(hashedPwd)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(dto),
                status = HttpStatus.NO_CONTENT
            )
            verify(pwdEncoder).encode(dto.newPwd)
            verify(userService).updatePassword(principal.user.id, hashedPwd, source)
        }
    }
}
