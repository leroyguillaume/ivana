package io.ivana.core

import java.time.Instant
import java.util.*

interface Event {
    val date: Instant
    val subjectId: UUID
    val number: Long
    val source: EventSource
}
