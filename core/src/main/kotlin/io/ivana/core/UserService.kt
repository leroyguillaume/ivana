package io.ivana.core

import java.util.*

interface UserService : EntityService<User> {
    fun create(content: UserEvent.Creation.Content, source: EventSource): User

    fun getByName(username: String): User

    fun updatePassword(id: UUID, newHashedPwd: String, source: EventSource)
}
