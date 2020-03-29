package io.ivana.api.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.ivana.dto.ErrorDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomAuthenticationEntryPoint(
    private val mapper: ObjectMapper
) : BasicAuthenticationEntryPoint() {
    private companion object {
        val Logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint::class.java)
    }

    override fun commence(req: HttpServletRequest, resp: HttpServletResponse, exception: AuthenticationException) {
        val remoteAddr = req.getHeader("X-Forwarded-For") ?: req.remoteAddr
        Logger.warn("Anonymous user ($remoteAddr) attempted to access ${req.requestURI}")
        resp.status = HttpStatus.UNAUTHORIZED.value()
        resp.contentType = MediaType.APPLICATION_JSON_VALUE
        mapper.writeValue(resp.outputStream, ErrorDto.Unauthorized)
    }
}
