package io.ivana.api.impl

import io.ivana.core.Person

class PersonAlreadyExistsException(
    val person: Person
) : RuntimeException()
