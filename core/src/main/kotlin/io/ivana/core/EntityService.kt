package io.ivana.core

import java.util.*

interface EntityService<E : Entity> {
    fun getAll(pageNo: Int, pageSize: Int): Page<E>

    fun getAllByIds(ids: Set<UUID>): Set<E>

    fun getById(id: UUID): E
}
