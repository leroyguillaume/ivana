package io.ivana.api.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
    authManager: AuthenticationManager,
    private val authService: AuthenticationService
) : BasicAuthenticationFilter(authManager) {
    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        val jwt = accessToken(req)
        if (jwt != null) {
            try {
                val principal = authService.principalFromJwt(jwt)
                SecurityContextHolder.getContext().authentication = CustomAuthentication(principal)
            } catch (exception: BadJwtException) {

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
