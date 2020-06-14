package io.ivana.core

import java.util.*

interface PersonEventRepository : EventRepository<PersonEvent> {
    fun saveCreationEvent(content: PersonEvent.Creation.Content, source: EventSource.User): PersonEvent.Creation

    fun saveDeletionEvent(personId: UUID, source: EventSource.User): PersonEvent.Deletion

    fun saveUpdateEvent(
        personId: UUID,
        content: PersonEvent.Update.Content,
        source: EventSource.User
    ): PersonEvent.Update
}
