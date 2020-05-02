package io.ivana.core

data class UserPermissions(
    val user: User,
    val permissions: Set<Permission>
)
