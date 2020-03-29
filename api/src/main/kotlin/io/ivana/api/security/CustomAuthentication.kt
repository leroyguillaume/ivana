package io.ivana.api.security

import org.springframework.security.core.Authentication

class CustomAuthentication(
    @get:JvmName("getUserPrincipal")
    val principal: UserPrincipal
) : Authentication {
    override fun getAuthorities() = principal.authorities

    override fun setAuthenticated(isAuthenticated: Boolean) {

    }

    override fun getName() = principal.username

    override fun getCredentials() = null

    override fun getPrincipal() = principal

    override fun isAuthenticated() = true

    override fun getDetails() = principal
}
