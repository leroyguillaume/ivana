package io.ivana.dto

import javax.validation.constraints.Size

data class PasswordUpdateDto(
    @get:Size(min = UserPasswordMinSize)
    val newPwd: String
)
