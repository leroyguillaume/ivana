package io.ivana.dto

import java.time.OffsetDateTime
import java.util.*

data class UserDto(
    val id: UUID,
    val name: String,
    val role: RoleDto,
    val creationDate: OffsetDateTime
) : EntityDto
