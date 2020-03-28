@file:Suppress("ClassName")

package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ivana.api.security.AccessTokenCookieName
import io.ivana.api.security.AuthenticationService
import io.ivana.api.security.BadCredentialsException
import io.ivana.api.security.Jwt
import io.ivana.dto.CredentialsDto
import io.ivana.dto.ErrorDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.net.InetAddress
import javax.servlet.http.Cookie

@SpringBootTest
@AutoConfigureMockMvc
internal class LoginControllerTest : AbstractControllerTest() {
    @MockBean
    private lateinit var authService: AuthenticationService

    @BeforeEach
    fun beforeEach() {
        reset(authService)
    }

    @Nested
    inner class login {
        private val creds = CredentialsDto(
            username = "admin",
            password = "changeit"
        )
        private val ip = InetAddress.getByName("127.0.0.1")
        private val jwt = Jwt(
            value = "jwt",
            expirationInSeconds = 60
        )
        private val localCookie = accessTokenCookie(Host, false)
        private val rpCookie = accessTokenCookie(ForwardedHost, true)

        @Test
        fun `should return 401 if credentials are invalid`() {
            whenever(authService.authenticate(creds.username, creds.password, ip))
                .thenAnswer { throw BadCredentialsException("") }
            callAndExpect(
                method = HttpMethod.POST,
                uri = LoginEndpoint,
                reqContent = mapper.writeValueAsString(creds),
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
            verify(authService).authenticate(creds.username, creds.password, ip)
        }

        @Test
        fun `should return 204 (local)`() {
            whenever(authService.authenticate(creds.username, creds.password, ip)).thenReturn(jwt)
            callAndExpect(
                method = HttpMethod.POST,
                uri = LoginEndpoint,
                reqContent = mapper.writeValueAsString(creds),
                status = HttpStatus.NO_CONTENT,
                cookies = listOf(localCookie)
            )
            verify(authService).authenticate(creds.username, creds.password, ip)
        }

        @Test
        fun `should return 204 (behind RP)`() {
            whenever(authService.authenticate(creds.username, creds.password, ip)).thenReturn(jwt)
            callAndExpect(
                method = HttpMethod.POST,
                uri = LoginEndpoint,
                headers = RpHeaders,
                reqContent = mapper.writeValueAsString(creds),
                status = HttpStatus.NO_CONTENT,
                cookies = listOf(rpCookie)
            )
            verify(authService).authenticate(creds.username, creds.password, ip)
        }

        private fun accessTokenCookie(host: String, secured: Boolean) =
            Cookie(AccessTokenCookieName, jwt.value).apply {
                domain = host
                maxAge = jwt.expirationInSeconds
                isHttpOnly = true
                path = "/"
                secure = secured
            }
    }

    @Nested
    inner class logout {
        private val localCookie = accessTokenCookie(Host, false)
        private val rpCookie = accessTokenCookie(ForwardedHost, true)

        @Test
        fun `should return 204 (local)`() {
            callAndExpect(
                method = HttpMethod.GET,
                uri = LogoutEndpoint,
                status = HttpStatus.NO_CONTENT,
                cookies = listOf(localCookie)
            )
        }

        @Test
        fun `should return 204 (behind RP)`() {
            callAndExpect(
                method = HttpMethod.GET,
                uri = LogoutEndpoint,
                headers = RpHeaders,
                status = HttpStatus.NO_CONTENT,
                cookies = listOf(rpCookie)
            )
        }

        private fun accessTokenCookie(host: String, secured: Boolean) =
            Cookie(AccessTokenCookieName, "").apply {
                domain = host
                maxAge = 0
                isHttpOnly = true
                path = "/"
                secure = secured
            }
    }
}
