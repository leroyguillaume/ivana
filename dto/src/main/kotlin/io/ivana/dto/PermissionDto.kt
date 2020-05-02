package io.ivana.dto

import com.fasterxml.jackson.annotation.JsonProperty

enum class PermissionDto {
    @JsonProperty("read")
    Read,

    @JsonProperty("update")
    Update,

    @JsonProperty("delete")
    Delete,

    @JsonProperty("update_permissions")
    UpdatePermissions;
}
