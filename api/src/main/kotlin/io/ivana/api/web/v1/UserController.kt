package io.ivana.api.web.v1

import io.ivana.api.security.UserPrincipal
import io.ivana.api.security.UserTargetType
import io.ivana.api.web.source
import io.ivana.core.Role
import io.ivana.core.UserEvent
import io.ivana.core.UserService
import io.ivana.dto.*
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

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
    @DeleteMapping("/{id:$UuidRegex}")
    @PreAuthorize("hasPermission(#id, '$UserTargetType', 'delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun delete(@PathVariable id: UUID, auth: Authentication, req: HttpServletRequest) {
        val principal = auth.principal as UserPrincipal
        userService.delete(id, req.source(principal))
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @ResponseStatus(HttpStatus.OK)
    fun getAll(
        @RequestParam(name = PageParamName, required = false, defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = SizeParamName, required = false, defaultValue = "10") @Min(1) size: Int,
        auth: Authentication
    ): PageDto<UserDto> {
        return userService.getAll(page, size).toDto { it.toDto() }
    }


    @GetMapping(MeEndpoint)
    @ResponseStatus(HttpStatus.OK)
    fun me(auth: Authentication) = (auth.principal as UserPrincipal).user.toDto()

    @GetMapping(SuggestEndpoint)
    @ResponseStatus(HttpStatus.OK)
    fun suggest(
        @RequestParam(name = QParamName) @NotBlank q: String,
        @RequestParam(name = CountParamName, required = false, defaultValue = "5") @Min(1) count: Int
    ) = userService.suggest(q.trim(), count).map { it.toDto() }

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
