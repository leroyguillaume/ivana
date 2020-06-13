package io.ivana.core

enum class Permission(
    val label: String
) {
    Read("read"),
    Update("update"),
    Delete("delete"),
    UpdatePermissions("update_permissions");

    companion object {
        fun fromLabel(label: String) = values().find { it.label == label }
            ?: throw IllegalArgumentException("Unknown permission '$label'")
    }
}
