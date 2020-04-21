package io.ivana.api.impl

import io.ivana.core.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserServiceImpl(
    override val repo: UserRepository,
    private val eventRepo: UserEventRepository
) : UserService, AbstractEntityService<User>() {
    private companion object {
        val Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    override val entityName = "User"

    @Transactional
    override fun create(content: UserEvent.Creation.Content, source: EventSource): User {
        val existingUser = repo.fetchByName(content.name)
        if (existingUser != null) {
            Logger.info("Unable to create user '${content.name}': it already exists")
            throw UserAlreadyExistsException(existingUser)
        }
        val event = eventRepo.saveCreationEvent(content, source)
        when (source) {
            is EventSource.System -> Logger.info("System created user '${content.name}' (${event.subjectId})")
            is EventSource.User ->
                Logger.info("User ${source.id} (${source.ip}) created user '${content.name}' (${event.subjectId})")
        }
        return repo.fetchById(event.subjectId)!!
    }

    @Transactional
    override fun delete(id: UUID, source: EventSource) {
        if (!repo.existsById(id)) {
            throw EntityNotFoundException("$entityName $id does not exist")
        }
        eventRepo.saveDeletionEvent(id, source)
        when (source) {
            is EventSource.System -> Logger.info("System deleted user $id")
            is EventSource.User -> Logger.info("User ${source.id} (${source.ip}) deleted user $id")
        }
    }

    override fun getByName(username: String) = repo.fetchByName(username)
        ?: throw EntityNotFoundException("User '$username' does not exist")

    @Transactional
    override fun updatePassword(id: UUID, newHashedPwd: String, source: EventSource) {
        if (!repo.existsById(id)) {
            throw EntityNotFoundException("User $id does not exist")
        }
        eventRepo.savePasswordUpdateEvent(id, newHashedPwd, source)
        when (source) {
            is EventSource.System -> Logger.info("System changed password of user $id")
            is EventSource.User -> Logger.info("User ${source.id} (${source.ip}) changed password of user $id")
        }
    }
}
