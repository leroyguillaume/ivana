package io.ivana.api.impl

import io.ivana.core.Entity
import io.ivana.core.EntityRepository
import io.ivana.core.EntityService
import io.ivana.core.Page
import java.util.*
import kotlin.math.ceil

abstract class AbstractEntityService<E : Entity> : EntityService<E> {
    protected abstract val repo: EntityRepository<E>
    protected abstract val entityName: String

    override fun getAll(pageNo: Int, pageSize: Int): Page<E> {
        val content = repo.fetchAll((pageNo - 1) * pageSize, pageSize)
        val itemsNb = repo.count()
        return Page(
            content = content,
            no = pageNo,
            totalItems = itemsNb,
            totalPages = ceil(itemsNb.toDouble() / pageSize.toDouble()).toInt()
        )
    }

    override fun getById(id: UUID) = repo.fetchById(id)
        ?: throw EntityNotFoundException("$entityName $id does not exist")
}
