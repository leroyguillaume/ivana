@file:Suppress("ClassName")

package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ivana.api.impl.UserAlreadyExistsException
import io.ivana.api.web.AbstractControllerTest
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
