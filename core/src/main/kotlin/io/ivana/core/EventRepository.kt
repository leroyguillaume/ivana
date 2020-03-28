package io.ivana.core

import java.util.*

interface EventRepository<E : Event> {
    fun fetch(subjectId: UUID, number: Long): E?
}
