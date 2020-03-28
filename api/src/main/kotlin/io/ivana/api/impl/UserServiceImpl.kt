package io.ivana.api.impl

import io.ivana.core.EntityNotFoundException
import io.ivana.core.UserRepository
import io.ivana.core.UserService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepo: UserRepository
) : UserService {
    override fun findByName(username: String) = userRepo.fetchByName(username)
        ?: throw EntityNotFoundException("User '$username' does not exist")
}
