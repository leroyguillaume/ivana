package io.ivana.api.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.ivana.api.web.remoteHost
import io.ivana.dto.ErrorDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.access.AccessDeniedHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomAccessDeniedHandler(
    private val mapper: ObjectMapper
) : AccessDeniedHandler {
    private companion object {
        val Logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint::class.java)
    }

    override fun handle(req: HttpServletRequest, resp: HttpServletResponse, exception: AccessDeniedException) {
        val principal = SecurityContextHolder.getContext().authentication.principal as UserPrincipal
        val remoteAddr = req.remoteHost()
        Logger.warn("User '${principal.username}' ($remoteAddr) attempted to access ${req.requestURI}")
        resp.status = HttpStatus.FORBIDDEN.value()
        resp.contentType = MediaType.APPLICATION_JSON_VALUE
        mapper.writeValue(resp.outputStream, ErrorDto.Forbidden)
    }
}
