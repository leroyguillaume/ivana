package io.ivana.api.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ivana.api.security.AuthenticationService
import io.ivana.api.security.UserPhotoAuthorizationRepository
import io.ivana.api.security.UserPrincipal
import io.ivana.core.*
import io.ivana.dto.ErrorDto
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import org.springframework.test.web.servlet.result.CookieResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.util.LinkedMultiValueMap
import java.io.File
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*
import javax.servlet.http.Cookie

@Suppress("SpringJavaAutowiredMembersInspection")
abstract class AbstractControllerTest {
    protected companion object {
        const val Host = "localhost"

        val RpRealIp = InetAddress.getByName("192.168.1.12")
        val RpHeaders = mapOf(
            "X-Real-IP" to listOf(RpRealIp.hostAddress),
            "X-Forwarded-Proto" to listOf("https")
        )
    }

    protected val jwt = "jwt"
    protected val userPrincipal = UserPrincipal(
        User(
            id = UUID.randomUUID(),
            name = "user",
            hashedPwd = "hashedPwd",
            role = Role.User,
            creationDate = OffsetDateTime.now()
        )
    )
    protected val adminPrincipal = UserPrincipal(
        User(
            id = UUID.randomUUID(),
            name = "admin",
            hashedPwd = "hashedPwd",
            role = Role.Admin,
            creationDate = OffsetDateTime.now()
        )
    )
    protected val superAdminPrincipal = UserPrincipal(
        User(
            id = UUID.randomUUID(),
            name = "superadmin",
            hashedPwd = "hashedPwd",
            role = Role.SuperAdmin,
            creationDate = OffsetDateTime.now()
        )
    )

    @Autowired
    protected lateinit var mapper: ObjectMapper

    @Autowired
    protected lateinit var mvc: MockMvc

    @MockBean
    protected lateinit var userPhotoAuthzRepo: UserPhotoAuthorizationRepository

    @MockBean
    protected lateinit var authService: AuthenticationService

    @MockBean
    protected lateinit var photoService: PhotoService

    @MockBean
    protected lateinit var userService: UserService

    @MockBean
    protected lateinit var pwdEncoder: BCryptPasswordEncoder

    @BeforeEach
    fun beforeEach() {
        reset(authService, photoService, userService, pwdEncoder)
    }

    protected fun accessTokenCookie() = Cookie(AccessTokenCookieName, jwt).apply {
        domain = Host
        maxAge = 60
        isHttpOnly = true
        path = "/"
        secure = false
    }

    protected fun authenticated(principal: UserPrincipal = userPrincipal, block: CallContext.() -> Unit) {
        CallContext(principal).apply {
            whenever(authService.principalFromJwt(jwt)).thenReturn(principal)
            block()
            verify(authService).principalFromJwt(jwt)
        }
    }

    protected fun callAndExpectDto(
        method: HttpMethod,
        uri: String,
        status: HttpStatus,
        reqContent: String? = null,
        respDto: Any? = null,
        contentType: MediaType = MediaType.APPLICATION_JSON,
        params: Map<String, List<String>> = mapOf(),
        reqHeaders: Map<String, List<String>> = mapOf(),
        reqCookies: List<Cookie> = listOf(),
        respCookies: List<Cookie> = listOf()
    ) {
        val request = request(method, uri)
            .params(LinkedMultiValueMap(params))
            .headers(HttpHeaders(LinkedMultiValueMap(reqHeaders)))
        if (reqContent != null) {
            request
                .contentType(contentType)
                .content(reqContent)
        }
        reqCookies.forEach { request.cookie(it) }
        val result = mvc.perform(request)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().`is`(status.value()))
        respCookies.forEach { result.andExpect(cookie().`is`(it)) }
        if (respDto != null) {
            result.andExpect(content().json(mapper.writeValueAsString(respDto)))
        }
    }

    protected fun callAndExpectFile(
        method: HttpMethod,
        uri: String,
        status: HttpStatus,
        expectedContentType: MediaType,
        expectedFile: File,
        reqContent: String? = null,
        contentType: MediaType = MediaType.APPLICATION_JSON,
        params: Map<String, List<String>> = mapOf(),
        reqHeaders: Map<String, List<String>> = mapOf(),
        reqCookies: List<Cookie> = listOf(),
        respCookies: List<Cookie> = listOf()
    ) {
        val request = request(method, uri)
            .params(LinkedMultiValueMap(params))
            .headers(HttpHeaders(LinkedMultiValueMap(reqHeaders)))
        if (reqContent != null) {
            request
                .contentType(contentType)
                .content(reqContent)
        }
        reqCookies.forEach { request.cookie(it) }
        val result = mvc.perform(request)
            .andExpect(status().`is`(status.value()))
            .andExpect(content().contentType(expectedContentType))
            .andExpect(content().bytes(expectedFile.readBytes()))
        respCookies.forEach { result.andExpect(cookie().`is`(it)) }
    }

    protected fun minErrorDto(parameter: String, min: Int) = ErrorDto.InvalidParameter(
        parameter = parameter,
        reason = "must be greater than or equal to $min"
    )

    protected fun multipartCallAndExpectDto(
        uri: String,
        status: HttpStatus,
        files: List<MockMultipartFile>,
        respDto: Any? = null,
        reqHeaders: Map<String, List<String>> = mapOf(),
        reqCookies: List<Cookie> = listOf(),
        respCookies: List<Cookie> = listOf()
    ) {
        val request = multipart(uri)
        files.forEach { request.file(it) }
        reqCookies.forEach { request.cookie(it) }
        val result = mvc.perform(request.headers(HttpHeaders(LinkedMultiValueMap(reqHeaders))))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().`is`(status.value()))
        respCookies.forEach { result.andExpect(cookie().`is`(it)) }
        if (respDto != null) {
            result.andExpect(content().json(mapper.writeValueAsString(respDto)))
        }
    }

    protected fun sizeErrorDto(parameter: String, min: Int = 0, max: Int = Int.MAX_VALUE) =
        ErrorDto.InvalidParameter(
            parameter = parameter,
            reason = "size must be between $min and $max"
        )

    protected fun typeMismatchErrorDto(parameter: String, type: String) = ErrorDto.InvalidParameter(
        parameter = parameter,
        reason = "must be $type"
    )

    private fun CookieResultMatchers.`is`(cookie: Cookie) = ResultMatcher { mvc ->
        exists(cookie.name).match(mvc)
        comment(cookie.name, cookie.comment).match(mvc)
        domain(cookie.name, cookie.domain).match(mvc)
        httpOnly(cookie.name, cookie.isHttpOnly).match(mvc)
        secure(cookie.name, cookie.secure).match(mvc)
        maxAge(cookie.name, cookie.maxAge).match(mvc)
        path(cookie.name, cookie.path).match(mvc)
        value(cookie.name, cookie.value).match(mvc)
        version(cookie.name, cookie.version).match(mvc)
    }

    protected class CallContext(
        val principal: UserPrincipal
    ) {
        val source = EventSource.User(principal.user.id, InetAddress.getByName("127.0.0.1"))
    }
}
