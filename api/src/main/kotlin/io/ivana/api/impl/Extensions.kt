package io.ivana.api.impl

import io.ivana.core.EventSource

internal fun EventSource.toData() = when (this) {
    is EventSource.System -> EventSourceData.System
    is EventSource.User -> EventSourceData.User(id, ip)
}
