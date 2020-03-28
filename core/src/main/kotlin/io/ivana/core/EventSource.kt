package io.ivana.core

import java.net.InetAddress
import java.util.*

sealed class EventSource {
    data class User(
        val id: UUID,
        val ip: InetAddress
    ) : EventSource()

    object System : EventSource()
}
