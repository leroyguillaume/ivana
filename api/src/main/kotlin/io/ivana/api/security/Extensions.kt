package io.ivana.api.security

import java.net.InetAddress
import javax.servlet.http.HttpServletRequest

internal const val Bearer = "Bearer"
internal const val AccessTokenCookieName = "access_token"

fun HttpServletRequest.remoteHost() = InetAddress.getByName(getHeader("X-Real-IP") ?: remoteAddr)
