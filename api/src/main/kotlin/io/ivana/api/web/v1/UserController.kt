package io.ivana.api.web.v1

import io.ivana.api.security.UserPrincipal
import io.ivana.api.web.source
import io.ivana.core.Role
import io.ivana.core.UserEvent
import io.ivana.core.UserService
import io.ivana.dto.PasswordUpdateDto
import io.ivana.dto.RoleDto
import io.ivana.dto.UserCreationDto
import io.ivana.dto.UserDto
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping(UserApiEndpoint)
@Validated
class UserController(
    private val userService: UserService,
    private val pwdEncoder: PasswordEncoder
) {
    @Transactional
    @PostMapping
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody @Valid creationDto: UserCreationDto,
        auth: Authentication,
        req: HttpServletRequest
    ): UserDto {
        val principal = auth.principal as UserPrincipal
        val content = UserEvent.Creation.Content(
            name = creationDto.name,
            hashedPwd = pwdEncoder.encode(creationDto.pwd),
            role = creationDto.role.toRole()
        )
        return userService.create(content, req.source(principal)).toDto()
    }

    @Transactional
    @PutMapping(PasswordUpdateEndpoint)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updatePassword(
        @RequestBody @Valid pwdUpdateDto: PasswordUpdateDto,
        auth: Authentication,
        req: HttpServletRequest
    ) {
        val principal = auth.principal as UserPrincipal
        userService.updatePassword(principal.user.id, pwdEncoder.encode(pwdUpdateDto.newPwd), req.source(principal))
    }

    private fun RoleDto.toRole() = when (this) {
        RoleDto.User -> Role.User
        RoleDto.Admin -> Role.Admin
        RoleDto.SuperAdmin -> Role.SuperAdmin
    }
}
