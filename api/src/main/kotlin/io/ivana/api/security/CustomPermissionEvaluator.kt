package io.ivana.api.security

import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import java.io.Serializable
import java.util.*

const val UserPhotoTargetType = "user_photo"

class CustomPermissionEvaluator(
    private val userPhotoAuthzRepo: UserPhotoAuthorizationRepository
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
        val permission = Permission.values().find { it.label == permissionLabel }
            ?: throw IllegalArgumentException("Unknown permission '$permissionLabel'")
        val principal = authentication.principal as UserPrincipal
        return when (targetType) {
            UserPhotoTargetType -> userPhotoAuthzRepo.fetch(principal.user.id, targetId).contains(permission)
            else -> throw IllegalArgumentException("Unsupported target type '$targetType'")
        }
    }
}
