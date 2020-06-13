package io.ivana.api.impl

import com.fasterxml.jackson.annotation.JsonProperty
import io.ivana.core.Permission

internal enum class PermissionData(
    val permission: Permission
) {
    @JsonProperty("read")
    Read(Permission.Read),

    @JsonProperty("update")
    Update(Permission.Update),

    @JsonProperty("delete")
    Delete(Permission.Delete),

    @JsonProperty("update_permissions")
    UpdatePermissions(Permission.UpdatePermissions)
}
