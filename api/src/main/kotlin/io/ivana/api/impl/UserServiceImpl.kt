package io.ivana.api.impl

import io.ivana.core.UserRepository
import io.ivana.core.UserService
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserServiceImpl(
    private val userRepo: UserRepository
) : UserService {
    override fun getById(id: UUID) = userRepo.fetchById(id) ?: throw EntityNotFoundException("User $id does not exist")

    override fun getByName(username: String) = userRepo.fetchByName(username)
        ?: throw EntityNotFoundException("User '$username' does not exist")
}
