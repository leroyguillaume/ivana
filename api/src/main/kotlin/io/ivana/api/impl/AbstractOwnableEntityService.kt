package io.ivana.api.impl

import io.ivana.core.*
import java.util.*
import kotlin.math.ceil

abstract class AbstractOwnableEntityService<E : OwnableEntity> : OwnableEntityService<E>, AbstractEntityService<E>() {
    abstract override val repo: OwnableEntityRepository<E>
    abstract val userRepo: UserRepository
    abstract val authzRepo: AuthorizationRepository

    override fun getAll(ownerId: UUID, pageNo: Int, pageSize: Int): Page<E> {
        val content = repo.fetchAll(ownerId, (pageNo - 1) * pageSize, pageSize)
        val itemsNb = repo.count(ownerId)
        return Page(
            content = content,
            no = pageNo,
            totalItems = itemsNb,
            totalPages = ceil(itemsNb.toDouble() / pageSize.toDouble()).toInt()
        )
    }

    override fun getAllPermissions(id: UUID, pageNo: Int, pageSize: Int): Page<SubjectPermissions> {
        val content = authzRepo.fetchAll(id, (pageNo - 1) * pageSize, pageSize)
        val itemsNb = authzRepo.count(id)
        return Page(
            content = content,
            no = pageNo,
            totalItems = itemsNb,
            totalPages = ceil(itemsNb.toDouble() / pageSize.toDouble()).toInt()
        )
    }

    override fun getPermissions(id: UUID, userId: UUID): Set<Permission> {
        if (!repo.existsById(id)) {
            throw EntityNotFoundException("$entityName $id does not exist")
        }
        if (!userRepo.existsById(userId)) {
            throw EntityNotFoundException("User $userId does not exist")
        }
        return authzRepo.fetch(userId, id) ?: emptySet()
    }
}
