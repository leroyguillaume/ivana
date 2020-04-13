package io.ivana.api.web

import io.ivana.api.security.UserPrincipal
import io.ivana.core.EventSource
import java.net.InetAddress
import javax.servlet.http.HttpServletRequest

const val RootApiEndpoint = "/api"

const val Bearer = "Bearer"
const val AccessTokenCookieName = "access_token"

fun HttpServletRequest.remoteHost() = InetAddress.getByName(getHeader("X-Real-IP") ?: remoteAddr)

fun HttpServletRequest.source(principal: UserPrincipal) = EventSource.User(
    id = principal.user.id,
    ip = remoteHost()
)
