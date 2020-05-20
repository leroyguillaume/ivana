@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.Permission
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

@Suppress("SpringJavaAutowiredMembersInspection")
internal abstract class AbstractAuthorizationRepositoryTest {
    @Autowired
    protected lateinit var jdbc: NamedParameterJdbcTemplate

    protected fun deleteUserAlbumAuthorizations(subjectId: UUID, resourceId: UUID) =
        deleteAuthorizations(
            subjectId = subjectId,
            resourceId = resourceId,
            tableName = UserAlbumAuthorizationRepositoryImpl.TableName,
            subjectIdColumnName = UserAlbumAuthorizationRepositoryImpl.UserIdColumnName,
            resourceIdColumnName = UserAlbumAuthorizationRepositoryImpl.AlbumIdColumnName
        )

    protected fun deleteUserPhotoAuthorizations(subjectId: UUID, resourceId: UUID) =
        deleteAuthorizations(
            subjectId = subjectId,
            resourceId = resourceId,
            tableName = UserPhotoAuthorizationRepositoryImpl.TableName,
            subjectIdColumnName = UserPhotoAuthorizationRepositoryImpl.UserIdColumnName,
            resourceIdColumnName = UserPhotoAuthorizationRepositoryImpl.PhotoIdColumnName
        )

    protected fun updateUserAlbumAuthorizations(subjectId: UUID, resourceId: UUID, vararg permissions: Permission) =
        updateAuthorization(
            subjectId = subjectId,
            resourceId = resourceId,
            tableName = UserAlbumAuthorizationRepositoryImpl.TableName,
            subjectIdColumnName = UserAlbumAuthorizationRepositoryImpl.UserIdColumnName,
            resourceIdColumnName = UserAlbumAuthorizationRepositoryImpl.AlbumIdColumnName,
            permissions = permissions
        )

    protected fun updateUserPhotoAuthorizations(subjectId: UUID, resourceId: UUID, vararg permissions: Permission) =
        updateAuthorization(
            subjectId = subjectId,
            resourceId = resourceId,
            tableName = UserPhotoAuthorizationRepositoryImpl.TableName,
            subjectIdColumnName = UserPhotoAuthorizationRepositoryImpl.UserIdColumnName,
            resourceIdColumnName = UserPhotoAuthorizationRepositoryImpl.PhotoIdColumnName,
            permissions = permissions
        )

    private fun deleteAuthorizations(
        subjectId: UUID,
        resourceId: UUID,
        tableName: String,
        subjectIdColumnName: String,
        resourceIdColumnName: String
    ) {
        jdbc.update(
            """
            DELETE FROM $tableName
            WHERE $subjectIdColumnName = :subject_id AND $resourceIdColumnName = :resource_id
            """,
            MapSqlParameterSource(mapOf("subject_id" to subjectId, "resource_id" to resourceId))
        )
    }

    private fun updateAuthorization(
        subjectId: UUID,
        resourceId: UUID,
        tableName: String,
        subjectIdColumnName: String,
        resourceIdColumnName: String,
        permissions: Array<out Permission>
    ) {
        val canRead = permissions.contains(Permission.Read)
        val canUpdate = permissions.contains(Permission.Update)
        val canDelete = permissions.contains(Permission.Delete)
        val canUpdatePermissions = permissions.contains(Permission.UpdatePermissions)
        jdbc.update(
            """
            INSERT INTO $tableName
            VALUES (
                :subject_id,
                :resource_id,
                :can_read,
                :can_update,
                :can_delete,
                :can_update_permissions
            )
            ON CONFLICT ($subjectIdColumnName, $resourceIdColumnName) DO UPDATE
            SET ${AbstractAuthorizationRepository.CanReadColumnName} = :can_read,
                ${AbstractAuthorizationRepository.CanUpdateColumnName} = :can_update,
                ${AbstractAuthorizationRepository.CanDeleteColumnName} = :can_delete,
                ${AbstractAuthorizationRepository.CanUpdatePermissionsColumnName} = :can_update_permissions
            """,
            MapSqlParameterSource(
                mapOf(
                    "can_read" to canRead,
                    "can_update" to canUpdate,
                    "can_delete" to canDelete,
                    "can_update_permissions" to canUpdatePermissions,
                    "subject_id" to subjectId,
                    "resource_id" to resourceId
                )
            )
        )
    }
}
