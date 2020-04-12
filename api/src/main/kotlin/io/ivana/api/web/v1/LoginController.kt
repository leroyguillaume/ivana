package io.ivana.api.web.v1

import io.ivana.api.security.AccessTokenCookieName
import io.ivana.api.security.AuthenticationService
import io.ivana.api.security.Bearer
import io.ivana.api.security.remoteHost
import io.ivana.dto.CredentialsDto
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.net.URI
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping
class LoginController(
    private val authService: AuthenticationService
) {
    @Transactional
    @PostMapping(LoginEndpoint)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun login(@RequestBody creds: CredentialsDto, req: HttpServletRequest, resp: HttpServletResponse) {
        val jwt = authService.authenticate(creds.username, creds.password, req.remoteHost())
        resp.addCookie(cookie(jwt.value, jwt.expirationInSeconds, req))
        resp.addHeader(HttpHeaders.AUTHORIZATION, "$Bearer ${jwt.value}")
    }

    @GetMapping(LogoutEndpoint)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.addCookie(cookie("", 0, req))
    }

    // Do nothing, just test if user is logged
    @GetMapping(LoginEndpoint)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun testLogin() {
    }

    private fun cookie(value: String, expirationInSeconds: Int, req: HttpServletRequest) =
        Cookie(AccessTokenCookieName, value).apply {
            secure = req.getHeader("X-Forwarded-Proto")?.let { it == "https" } ?: req.isSecure
            domain = req.getHeader("X-Forwarded-Host") ?: URI(req.requestURL.toString()).host
            isHttpOnly = true
            maxAge = expirationInSeconds
            path = "/"
        }
}
