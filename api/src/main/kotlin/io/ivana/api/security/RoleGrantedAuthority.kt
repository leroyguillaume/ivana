package io.ivana.api.security

import io.ivana.core.Role
import org.springframework.security.core.GrantedAuthority

data class RoleGrantedAuthority(
    val role: Role
) : GrantedAuthority {
    override fun getAuthority() = role.label
}
