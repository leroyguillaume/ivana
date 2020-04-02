package io.ivana.api.security

enum class Permission {
    Read,
    Update,
    Delete;

    val label = name.toLowerCase()
}
