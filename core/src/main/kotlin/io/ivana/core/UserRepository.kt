package io.ivana.core

interface UserRepository : EntityRepository<User> {
    fun fetchByName(username: String): User?
}
