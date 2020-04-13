package io.ivana.dto

import com.fasterxml.jackson.annotation.JsonProperty

enum class RoleDto {
    @JsonProperty("user")
    User,

    @JsonProperty("admin")
    Admin,

    @JsonProperty("super_admin")
    SuperAdmin
}
