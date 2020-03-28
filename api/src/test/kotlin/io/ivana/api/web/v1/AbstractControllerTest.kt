package io.ivana.api.web.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import org.springframework.test.web.servlet.result.CookieResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.util.LinkedMultiValueMap
import javax.servlet.http.Cookie

@Suppress("SpringJavaAutowiredMembersInspection")
internal abstract class AbstractControllerTest {
    protected companion object {
        const val Host = "localhost:8080"
        const val ForwardedHost = "ivana.io"

        val StdHeaders = mapOf(
            HttpHeaders.HOST to listOf(Host)
        )
        val RpHeaders = mapOf(
            "X-Forwarded-Host" to listOf(ForwardedHost),
            "X-Forwarded-Proto" to listOf("https")
        )
    }

    @Autowired
    protected lateinit var mapper: ObjectMapper

    @Autowired
    protected lateinit var mvc: MockMvc

    protected fun callAndExpect(
        method: HttpMethod,
        uri: String,
        status: HttpStatus,
        reqContent: String? = null,
        respDto: Any? = null,
        contentType: MediaType = MediaType.APPLICATION_JSON,
        headers: Map<String, List<String>> = mapOf(),
        cookies: List<Cookie> = listOf()
    ) {
        val request = request(method, uri)
            .headers(HttpHeaders(LinkedMultiValueMap(headers + StdHeaders)))
        if (reqContent != null) {
            request
                .contentType(contentType)
                .content(reqContent)
        }
        val result = mvc.perform(request)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().`is`(status.value()))
        cookies.forEach { result.andExpect(cookie().`is`(it)) }
        if (respDto != null) {
            result.andExpect(content().json(mapper.writeValueAsString(respDto)))
        }
    }

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
}
