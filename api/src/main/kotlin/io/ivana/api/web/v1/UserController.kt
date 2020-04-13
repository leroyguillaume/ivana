package io.ivana.api.web.v1

import io.ivana.api.security.UserPrincipal
import io.ivana.core.UserService
import io.ivana.dto.PasswordUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(UserApiEndpoint)
class UserController(
    private val userService: UserService
) {
    @Transactional
    @PutMapping(PasswordUpdateEndpoint)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updatePassword(@RequestBody pwdUpdateDto: PasswordUpdateDto, auth: Authentication, req: HttpServletRequest) {
        val principal = auth.principal as UserPrincipal
        userService.updatePassword(principal.user.id, pwdUpdateDto.newPwd, userSource(req, principal))
    }
}
