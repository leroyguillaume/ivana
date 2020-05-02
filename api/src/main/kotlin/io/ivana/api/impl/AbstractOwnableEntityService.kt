package io.ivana.api.impl

import io.ivana.core.*
import java.util.*
import kotlin.math.ceil

abstract class AbstractOwnableEntityService<E : OwnableEntity> : OwnableEntityService<E>, AbstractEntityService<E>() {
    abstract override val repo: OwnableEntityRepository<E>
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

    override fun getPermissions(id: UUID, pageNo: Int, pageSize: Int): Page<SubjectPermissions> {
        val content = authzRepo.fetchAll(id, (pageNo - 1) * pageSize, pageSize)
        val itemsNb = authzRepo.count(id)
        return Page(
            content = content,
            no = pageNo,
            totalItems = itemsNb,
            totalPages = ceil(itemsNb.toDouble() / pageSize.toDouble()).toInt()
        )
    }
}
