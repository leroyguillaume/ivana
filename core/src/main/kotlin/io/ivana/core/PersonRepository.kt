package io.ivana.core

import java.util.*

interface PersonRepository : EntityRepository<Person> {
    fun fetchByName(lastName: String, firstName: String): Person?

    fun fetchOn(photoId: UUID): List<Person>

    fun suggest(name: String, count: Int): List<Person>
}
