package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ivana.api.security.AccessTokenCookieName
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
import javax.servlet.http.Cookie

@SpringBootTest
@AutoConfigureMockMvc
internal class ErrorControllerTest : AbstractControllerTest() {
    @Test
    fun `should return 404 if endpoint does not exist (auth by header)`() {
        val jwt = "jwt"
        whenever(authService.usernameFromJwt(jwt)).thenReturn("admin")
        callAndExpect(
            method = HttpMethod.GET,
            uri = "/",
            reqHeaders = mapOf(HttpHeaders.AUTHORIZATION to listOf("$Bearer $jwt")),
            status = HttpStatus.NOT_FOUND,
            respDto = ErrorDto.NotFound
        )
        verify(authService).usernameFromJwt(jwt)
    }

    @Test
    fun `should return 404 if endpoint does not exist (auth by cookie)`() {
        val jwt = "jwt"
        whenever(authService.usernameFromJwt(jwt)).thenReturn("admin")
        callAndExpect(
            method = HttpMethod.GET,
            uri = "/",
            reqCookies = listOf(accessTokenCookie(jwt)),
            status = HttpStatus.NOT_FOUND,
            respDto = ErrorDto.NotFound
        )
        verify(authService).usernameFromJwt(jwt)
    }

    @Test
    fun `should return 415 if content type is invalid`() {
        callAndExpect(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            contentType = MediaType.APPLICATION_PDF,
            reqContent = "{}",
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            respDto = ErrorDto.InvalidContentType(setOf(MediaType.APPLICATION_JSON_VALUE))
        )
    }

    @Test
    fun `should return 401 if no authentication`() {
        callAndExpect(
            method = HttpMethod.GET,
            uri = "/",
            status = HttpStatus.UNAUTHORIZED,
            respDto = ErrorDto.Unauthorized
        )
    }

    @Test
    fun `should return 401 if bad jwt (header)`() {
        val jwt = "jwt"
        whenever(authService.usernameFromJwt(jwt)).thenAnswer { throw BadJwtException("") }
        callAndExpect(
            method = HttpMethod.GET,
            uri = "/",
            reqHeaders = mapOf(HttpHeaders.AUTHORIZATION to listOf("$Bearer $jwt")),
            status = HttpStatus.UNAUTHORIZED,
            respDto = ErrorDto.Unauthorized
        )
        verify(authService).usernameFromJwt(jwt)
    }

    @Test
    fun `should return 401 if bad jwt (cookie)`() {
        val jwt = "jwt"
        whenever(authService.usernameFromJwt(jwt)).thenAnswer { throw BadJwtException("") }
        callAndExpect(
            method = HttpMethod.GET,
            uri = "/",
            reqCookies = listOf(accessTokenCookie(jwt)),
            status = HttpStatus.UNAUTHORIZED,
            respDto = ErrorDto.Unauthorized
        )
        verify(authService).usernameFromJwt(jwt)
    }

    @Test
    fun `should return 400 if content is empty`() {
        callAndExpect(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            reqContent = "",
            status = HttpStatus.BAD_REQUEST,
            respDto = ErrorDto.MalformedRequest
        )
    }

    @Test
    fun `should return 400 if content is malformed`() {
        callAndExpect(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            reqContent = "{",
            status = HttpStatus.BAD_REQUEST,
            respDto = ErrorDto.MalformedRequest
        )
    }

    @Test
    fun `should return 400 if missing parameter`() {
        callAndExpect(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            reqContent = "{}",
            status = HttpStatus.BAD_REQUEST,
            respDto = ErrorDto.MissingParameter("username")
        )
    }

    private fun accessTokenCookie(value: String) = Cookie(AccessTokenCookieName, value).apply {
        domain = Host
        maxAge = 60
        isHttpOnly = true
        path = "/"
        secure = false
    }
}
