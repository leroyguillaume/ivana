package io.ivana.api.web

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ivana.api.impl.EntityNotFoundException
import io.ivana.api.impl.ResourcesNotFoundException
import io.ivana.api.security.BadJwtException
import io.ivana.api.web.v1.*
import io.ivana.core.Permission
import io.ivana.dto.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
internal class ErrorControllerTest : AbstractControllerTest() {
    @Test
    fun `should return 415 if content type is invalid`() {
        callAndExpectDto(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            contentType = MediaType.APPLICATION_PDF,
            reqContent = "{}",
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            respDto = ErrorDto.UnsupportedMediaType(setOf(MediaType.APPLICATION_JSON_VALUE))
        )
    }

    @Test
    fun `should return 401 if bad jwt (header)`() {
        whenever(authService.principalFromJwt(jwt)).thenAnswer { throw BadJwtException("") }
        callAndExpectDto(
            method = HttpMethod.GET,
            uri = PhotoApiEndpoint,
            reqHeaders = mapOf(HttpHeaders.AUTHORIZATION to listOf("$Bearer $jwt")),
            status = HttpStatus.UNAUTHORIZED,
            respDto = ErrorDto.Unauthorized
        )
        verify(authService).principalFromJwt(jwt)
    }

    @Test
    fun `should return 401 if bad jwt (cookie)`() {
        whenever(authService.principalFromJwt(jwt)).thenAnswer { throw BadJwtException("") }
        callAndExpectDto(
            method = HttpMethod.GET,
            uri = PhotoApiEndpoint,
            reqCookies = listOf(accessTokenCookie()),
            status = HttpStatus.UNAUTHORIZED,
            respDto = ErrorDto.Unauthorized
        )
        verify(authService).principalFromJwt(jwt)
    }

    @Test
    fun `should return 400 if content is empty`() {
        callAndExpectDto(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            reqContent = "",
            status = HttpStatus.BAD_REQUEST,
            respDto = ErrorDto.MalformedRequest
        )
    }

    @Test
    fun `should return 400 if content is malformed`() {
        callAndExpectDto(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            reqContent = "{",
            status = HttpStatus.BAD_REQUEST,
            respDto = ErrorDto.MalformedRequest
        )
    }

    @Test
    fun `should return 400 if missing parameter`() {
        callAndExpectDto(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            reqContent = "{}",
            status = HttpStatus.BAD_REQUEST,
            respDto = ErrorDto.MissingParameter("username")
        )
    }

    @Test
    fun `should return 400 if parameter type mismatch`() = authenticated {
        callAndExpectDto(
            method = HttpMethod.GET,
            uri = "$PhotoApiEndpoint/${UUID.randomUUID()}",
            params = mapOf(NavigableParamName to listOf("a")),
            reqCookies = listOf(accessTokenCookie()),
            status = HttpStatus.BAD_REQUEST,
            respDto = typeMismatchErrorDto(NavigableParamName, "boolean")
        )
    }

    @Test
    fun `should return 405`() = authenticated {
        callAndExpectDto(
            method = HttpMethod.POST,
            uri = "$UserApiEndpoint$PasswordUpdateEndpoint",
            reqCookies = listOf(accessTokenCookie()),
            reqContent = mapper.writeValueAsString(PasswordUpdateDto("changeit")),
            status = HttpStatus.METHOD_NOT_ALLOWED,
            respDto = ErrorDto.MethodNotAllowed
        )
    }

    @Test
    fun `should return 404`() = authenticated(superAdminPrincipal) {
        val id = UUID.randomUUID()
        whenever(userService.delete(id, source)).thenAnswer { throw EntityNotFoundException("") }
        callAndExpectDto(
            method = HttpMethod.DELETE,
            uri = "$UserApiEndpoint/$id",
            reqCookies = listOf(accessTokenCookie()),
            status = HttpStatus.NOT_FOUND,
            respDto = ErrorDto.NotFound
        )
    }

    @Test
    fun `should return 400 if user does not exist`() = authenticated {
        val photoId = UUID.randomUUID()
        val dto = PhotoUpdatePermissionsDto(
            permissionsToAdd = setOf(
                SubjectPermissionsUpdateDto(
                    subjectId = UUID.randomUUID(),
                    permissions = setOf(PermissionDto.Read)
                )
            )
        )
        val usersIds = (dto.permissionsToAdd + dto.permissionsToRemove).map { it.subjectId }.toSet()
        whenever(userPhotoAuthzRepo.fetch(principal.user.id, photoId)).thenReturn(setOf(Permission.Update))
        whenever(userService.getAllByIds(usersIds)).thenAnswer { throw ResourcesNotFoundException.User(usersIds) }
        callAndExpectDto(
            method = HttpMethod.PUT,
            uri = "$PhotoApiEndpoint/$photoId$PermissionsEndpoint",
            reqCookies = listOf(accessTokenCookie()),
            reqContent = mapper.writeValueAsString(dto),
            status = HttpStatus.BAD_REQUEST,
            respDto = ErrorDto.ResourcesNotFound(ErrorDto.ResourcesNotFound.Type.User, usersIds)
        )
        verify(userPhotoAuthzRepo).fetch(principal.user.id, photoId)
        verify(userService).getAllByIds(usersIds)
    }
}
