package io.ivana.api.impl

import com.fasterxml.jackson.annotation.JsonProperty
import io.ivana.core.Role

private const val UserSqlValue = "user"
private const val AdminSqlValue = "admin"
private const val SuperAdminSqlValue = "super_admin"

internal enum class RoleData(
    val role: Role,
    val sqlValue: String
) {
    @JsonProperty(UserSqlValue)
    User(Role.User, UserSqlValue),

    @JsonProperty(AdminSqlValue)
    Admin(Role.Admin, AdminSqlValue),

    @JsonProperty(SuperAdminSqlValue)
    SuperAdmin(Role.SuperAdmin, SuperAdminSqlValue),
}
