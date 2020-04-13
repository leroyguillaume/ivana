package io.ivana.api.impl

import io.ivana.core.EventSource
import io.ivana.core.UserEventRepository
import io.ivana.core.UserRepository
import io.ivana.core.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserServiceImpl(
    private val userRepo: UserRepository,
    private val userEventRepo: UserEventRepository,
    private val pwdEncoder: PasswordEncoder
) : UserService {
    private companion object {
        val Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    override fun getById(id: UUID) = userRepo.fetchById(id) ?: throw EntityNotFoundException("User $id does not exist")

    override fun getByName(username: String) = userRepo.fetchByName(username)
        ?: throw EntityNotFoundException("User '$username' does not exist")

    override fun updatePassword(id: UUID, newPwd: String, source: EventSource) {
        if (!userRepo.existsById(id)) {
            throw EntityNotFoundException("User $id does not exist")
        }
        userEventRepo.savePasswordUpdateEvent(id, pwdEncoder.encode(newPwd), source)
        when (source) {
            is EventSource.System -> Logger.info("System changed password of user $id")
            is EventSource.User -> Logger.info("User ${source.id} (${source.ip}) changed password of user $id")
        }
    }
}
