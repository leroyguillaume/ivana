package io.ivana.core

import java.util.*

interface EntityService<E : Entity> {
    fun getById(id: UUID): E
}
