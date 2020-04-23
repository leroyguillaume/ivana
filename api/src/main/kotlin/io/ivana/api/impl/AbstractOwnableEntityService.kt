package io.ivana.api.impl

import io.ivana.core.OwnableEntity
import io.ivana.core.OwnableEntityRepository
import io.ivana.core.OwnableEntityService
import io.ivana.core.Page
import java.util.*
import kotlin.math.ceil

abstract class AbstractOwnableEntityService<E : OwnableEntity> : OwnableEntityService<E>, AbstractEntityService<E>() {
    abstract override val repo: OwnableEntityRepository<E>

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
}
