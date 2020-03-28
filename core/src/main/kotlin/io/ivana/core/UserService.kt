package io.ivana.core

interface UserService {
    fun findByName(username: String): User
}
