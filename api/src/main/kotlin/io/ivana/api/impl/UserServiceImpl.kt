package io.ivana.api.impl

import io.ivana.core.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserServiceImpl(
    private val userRepo: UserRepository,
    private val userEventRepo: UserEventRepository
) : UserService {
    private companion object {
        val Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    @Transactional
    override fun create(content: UserEvent.Creation.Content, source: EventSource): User {
        val existingUser = userRepo.fetchByName(content.name)
        if (existingUser != null) {
            Logger.info("Unable to create user '${content.name}': it already exists")
            throw UserAlreadyExistsException(existingUser)
        }
        val event = userEventRepo.saveCreationEvent(content, source)
        when (source) {
            is EventSource.System -> Logger.info("System created user '${content.name}' (${event.subjectId})")
            is EventSource.User ->
                Logger.info("User ${source.id} (${source.ip}) created user '${content.name}' (${event.subjectId})")
        }
        return userRepo.fetchById(event.subjectId)!!
    }

    override fun getById(id: UUID) = userRepo.fetchById(id) ?: throw EntityNotFoundException("User $id does not exist")

    override fun getByName(username: String) = userRepo.fetchByName(username)
        ?: throw EntityNotFoundException("User '$username' does not exist")

    @Transactional
    override fun updatePassword(id: UUID, newHashedPwd: String, source: EventSource) {
        if (!userRepo.existsById(id)) {
            throw EntityNotFoundException("User $id does not exist")
        }
        userEventRepo.savePasswordUpdateEvent(id, newHashedPwd, source)
        when (source) {
            is EventSource.System -> Logger.info("System changed password of user $id")
            is EventSource.User -> Logger.info("User ${source.id} (${source.ip}) changed password of user $id")
        }
    }
}
