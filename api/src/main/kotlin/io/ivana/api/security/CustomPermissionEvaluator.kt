package io.ivana.api.security

import io.ivana.core.*
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import java.io.Serializable
import java.util.*

const val AlbumTargetType = "album"
const val PhotoTargetType = "photo"
const val UserTargetType = "user"

class CustomPermissionEvaluator(
    private val userPhotoAuthzRepo: UserPhotoAuthorizationRepository,
    private val userAlbumAuthzRepo: UserAlbumAuthorizationRepository,
    private val userRepo: UserRepository
) : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication, targetDomainObject: Any, permission: Any): Boolean {
        throw UnsupportedOperationException("Missing target type")
    }

    override fun hasPermission(
        authentication: Authentication,
        targetId: Serializable,
        targetType: String,
        permissionLabel: Any
    ): Boolean {
        if (targetId !is UUID) {
            throw IllegalArgumentException("$targetId is not an UUID")
        }
        val permission = Permission.values().find { it.label() == permissionLabel }
            ?: throw IllegalArgumentException("Unknown permission '$permissionLabel'")
        val principal = authentication.principal as UserPrincipal
        return when (targetType) {
            PhotoTargetType -> checkPhotoAuthz(principal, targetId, permission)
            UserTargetType -> checkUserAuthz(principal, targetId, permission)
            AlbumTargetType -> checkAlbumAuthz(principal, targetId, permission)
            else -> throw IllegalArgumentException("Unsupported target type '$targetType'")
        }
    }

    private fun checkAlbumAuthz(principal: UserPrincipal, targetId: UUID, permission: Permission) =
        userAlbumAuthzRepo.fetch(principal.user.id, targetId).contains(permission)

    private fun checkPhotoAuthz(principal: UserPrincipal, targetId: UUID, permission: Permission) =
        userPhotoAuthzRepo.fetch(principal.user.id, targetId).contains(permission)

    private fun checkUserAuthz(principal: UserPrincipal, targetId: UUID, permission: Permission) = when (permission) {
        Permission.Delete -> principal.user.id != targetId && (principal.user.role == Role.SuperAdmin ||
                userRepo.fetchById(targetId).let { it != null && principal.user.role > it.role })
        else -> TODO()
    }

    private fun Permission.label() = when (this) {
        Permission.Read -> "read"
        Permission.Update -> "update"
        Permission.Delete -> "delete"
        Permission.UpdatePermissions -> "update_permissions"
    }
}
