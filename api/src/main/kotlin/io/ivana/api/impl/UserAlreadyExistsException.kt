package io.ivana.api.impl

import io.ivana.core.User

class UserAlreadyExistsException(
    val user: User
) : RuntimeException()
