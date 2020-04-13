package io.ivana.core

import java.util.*

interface UserEventRepository : EventRepository<UserEvent> {
    fun saveCreationEvent(content: UserEvent.Creation.Content, source: EventSource): UserEvent.Creation

    fun saveLoginEvent(source: EventSource.User): UserEvent.Login

    fun savePasswordUpdateEvent(userId: UUID, newHashedPwd: String, source: EventSource): UserEvent.PasswordUpdate
}
