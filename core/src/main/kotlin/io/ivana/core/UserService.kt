package io.ivana.core

interface UserService : EntityService<User> {
    fun getByName(username: String): User
}
