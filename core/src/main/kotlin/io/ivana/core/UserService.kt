package io.ivana.core

import java.util.*

interface UserService : EntityService<User> {
    fun getByName(username: String): User

    fun updatePassword(id: UUID, newPwd: String, source: EventSource)
}
