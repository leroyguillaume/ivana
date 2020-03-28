package io.ivana.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ivana.api.config.AuthenticationProperties
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
    authManager: AuthenticationManager,
    private val props: AuthenticationProperties
) : BasicAuthenticationFilter(authManager) {
    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        val jwt = accessToken(req)
        if (jwt != null) {
            val username = JWT.require(Algorithm.HMAC512(props.secret))
                .build()
                .verify(jwt)
                .subject
            if (username != null) {
                SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
                    username, null, emptyList()
                )
            }
        }
        chain.doFilter(req, resp)
    }

    private fun accessToken(req: HttpServletRequest): String? {
        val authHeaderValue = req.getHeader(HttpHeaders.AUTHORIZATION)
        return if (authHeaderValue == null || !authHeaderValue.startsWith(Bearer)) {
            (req.cookies ?: emptyArray())
                .find { it.name == AccessTokenCookieName }
                ?.value
        } else {
            authHeaderValue.substring(Bearer.length + 1)
        }
    }
}
