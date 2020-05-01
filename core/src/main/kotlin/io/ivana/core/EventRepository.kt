package io.ivana.core

interface EventRepository<E : Event> {
    fun fetch(number: Long): E?
}
