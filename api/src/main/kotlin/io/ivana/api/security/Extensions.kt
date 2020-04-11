package io.ivana.api.security

import javax.servlet.http.HttpServletRequest

internal const val Bearer = "Bearer"
internal const val AccessTokenCookieName = "access_token"

internal fun HttpServletRequest.remoteHost() = getHeader("X-Real-IP") ?: remoteAddr
