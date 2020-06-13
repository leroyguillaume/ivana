package io.ivana.api.impl

class UnknownPermissionException(
    val permLabel: String
) : RuntimeException()
