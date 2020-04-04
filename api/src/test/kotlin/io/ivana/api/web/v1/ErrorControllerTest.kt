package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ivana.api.security.BadJwtException
import io.ivana.api.security.Bearer
import io.ivana.dto.ErrorDto
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest
@AutoConfigureMockMvc
internal class ErrorControllerTest : AbstractControllerTest() {
    @Test
    fun `should return 404 if endpoint does not exist (auth by header)`() = authenticated {
        callAndExpectDto(
            method = HttpMethod.GET,
            uri = "/",
            reqHeaders = mapOf(HttpHeaders.AUTHORIZATION to listOf("$Bearer $jwt")),
            status = HttpStatus.NOT_FOUND,
            respDto = ErrorDto.NotFound
        )
    }

    @Test
    fun `should return 404 if endpoint does not exist (auth by cookie)`() = authenticated {
        callAndExpectDto(
            method = HttpMethod.GET,
            uri = "/",
            reqCookies = listOf(accessTokenCookie()),
            status = HttpStatus.NOT_FOUND,
            respDto = ErrorDto.NotFound
        )
    }

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
            uri = "/",
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
            uri = "/",
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
}
