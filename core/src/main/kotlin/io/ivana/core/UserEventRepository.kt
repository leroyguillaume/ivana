package io.ivana.core

interface UserEventRepository : EventRepository<UserEvent> {
    fun saveCreationEvent(content: UserEvent.Creation.Content, source: EventSource): UserEvent.Creation

    fun saveLoginEvent(source: EventSource.User): UserEvent.Login
}
