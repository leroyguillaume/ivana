package io.ivana.core

import java.util.*

interface PersonService : EntityService<Person> {
    fun create(content: PersonEvent.Creation.Content, source: EventSource.User): Person

    fun delete(id: UUID, source: EventSource.User)

    fun suggest(name: String, count: Int): List<Person>

    fun update(id: UUID, content: PersonEvent.Update.Content, source: EventSource.User): Person
}
