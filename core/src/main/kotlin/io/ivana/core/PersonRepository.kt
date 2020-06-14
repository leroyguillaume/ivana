package io.ivana.core

interface PersonRepository : EntityRepository<Person> {
    fun fetchByName(lastName: String, firstName: String): Person?

    fun suggest(name: String, count: Int): List<Person>
}
