package io.ivana.core

import java.time.OffsetDateTime
import java.util.*

interface Event {
    val date: OffsetDateTime
    val subjectId: UUID
    val number: Long
    val source: EventSource
}
