package io.ivana.api.impl

import java.util.*

class PeopleAlreadyOnPhotoException(
    val peopleIds: Set<UUID>
) : RuntimeException()
