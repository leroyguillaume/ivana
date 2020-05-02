package io.ivana.core

import java.util.*

interface OwnableEntityService<E : OwnableEntity> : EntityService<E> {
    fun getAll(ownerId: UUID, pageNo: Int, pageSize: Int): Page<E>

    fun getPermissions(id: UUID, pageNo: Int, pageSize: Int): Page<SubjectPermissions>
}
