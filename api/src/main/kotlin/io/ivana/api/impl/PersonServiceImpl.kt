package io.ivana.api.impl

import io.ivana.core.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class PersonServiceImpl(
    override val repo: PersonRepository,
    private val eventRepo: PersonEventRepository
) : PersonService, AbstractEntityService<Person>() {
    internal companion object {
        private val Logger = LoggerFactory.getLogger(PersonServiceImpl::class.java)
    }

    override val entityName = "Person"

    @Transactional
    override fun create(content: PersonEvent.Creation.Content, source: EventSource.User): Person {
        val existingPerson = repo.fetchByName(content.lastName, content.firstName)
        if (existingPerson != null) {
            Logger.info("Unable to create person '${content.firstName} ${content.lastName}': it already exists")
            throw PersonAlreadyExistsException(existingPerson)
        }
        val event = eventRepo.saveCreationEvent(content, source)
        Logger.info("Person ${source.id} (${source.ip}) created person '${content.firstName} ${content.lastName}' (${event.subjectId})")
        return repo.fetchById(event.subjectId)!!
    }

    @Transactional
    override fun delete(id: UUID, source: EventSource.User) {
        checkPersonExists(id)
        eventRepo.saveDeletionEvent(id, source)
        Logger.info("User ${source.id} (${source.ip}) deleted person $id")
    }

    override fun suggest(name: String, count: Int) = repo.suggest(name, count)

    @Transactional
    override fun update(id: UUID, content: PersonEvent.Update.Content, source: EventSource.User): Person {
        checkPersonExists(id)
        val existingPerson = repo.fetchByName(content.lastName, content.firstName)
        if (existingPerson != null && existingPerson.id != id) {
            Logger.info("Unable to update person '${content.firstName} ${content.lastName}': it already exists")
            throw PersonAlreadyExistsException(existingPerson)
        }
        eventRepo.saveUpdateEvent(id, content, source)
        Logger.info("User ${source.id} (${source.ip}) updated person $id")
        return getById(id)
    }

    override fun throwResourcesNotFoundException(ids: Set<UUID>) {
        throw ResourcesNotFoundException.Person(ids)
    }

    private fun checkPersonExists(id: UUID) {
        if (!repo.existsById(id)) {
            throw EntityNotFoundException("$entityName $id does not exist")
        }
    }
}
