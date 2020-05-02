package io.ivana.api.impl

import com.fasterxml.jackson.annotation.JsonProperty
import io.ivana.core.Permission

private const val ReadSqlValue = "read"
private const val UpdateSqlValue = "update"
private const val DeleteSqlValue = "delete"
private const val UpdatePermissionsSqlValue = "update_permissions"

internal enum class PermissionData(
    val permission: Permission,
    val sqlValue: String
) {
    @JsonProperty(ReadSqlValue)
    Read(Permission.Read, ReadSqlValue),

    @JsonProperty(UpdateSqlValue)
    Update(Permission.Update, UpdateSqlValue),

    @JsonProperty(DeleteSqlValue)
    Delete(Permission.Delete, DeleteSqlValue),

    @JsonProperty(UpdatePermissionsSqlValue)
    UpdatePermissions(Permission.UpdatePermissions, UpdatePermissionsSqlValue)
}
