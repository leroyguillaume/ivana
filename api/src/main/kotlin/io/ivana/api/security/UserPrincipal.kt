package io.ivana.api.security

import io.ivana.core.User
import org.springframework.security.core.userdetails.UserDetails

data class UserPrincipal(
    val user: User
) : UserDetails {
    override fun getAuthorities() = setOf(RoleGrantedAuthority(user.role))

    override fun isEnabled() = true

    override fun getUsername() = user.name

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = user.hashedPwd

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true
}
