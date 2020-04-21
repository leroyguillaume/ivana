package io.ivana.api.config

import io.ivana.api.security.CustomPermissionEvaluator
import io.ivana.api.security.UserPhotoAuthorizationRepository
import io.ivana.core.UserRepository
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class MethodSecurityConfiguration(
    private val userPhotoAuthzRepo: UserPhotoAuthorizationRepository,
    private val userRepo: UserRepository
) : GlobalMethodSecurityConfiguration() {
    override fun createExpressionHandler() = DefaultMethodSecurityExpressionHandler().apply {
        setPermissionEvaluator(CustomPermissionEvaluator(userPhotoAuthzRepo, userRepo))
    }
}
