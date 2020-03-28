@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.EventSource
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.util.*

internal class EventSourceDataTest {
    @Nested
    inner class System {
        @Nested
        inner class toSource {
            @Test
            fun source() {
                EventSourceData.System.toSource() shouldBe EventSource.System
            }
        }

        @Nested
        inner class Json : JsonTest(
            filename = "event-data/source/system.json",
            expectedValue = EventSourceData.System,
            deserializeAs = typeOf<EventSourceData>()
        )
    }

    @Nested
    inner class User {
        @Nested
        inner class toSource {
            private val id = UUID.randomUUID()
            private val ip = InetAddress.getByName("127.0.0.1")

            @Test
            fun source() {
                EventSourceData.User(id, ip).toSource() shouldBe EventSource.User(id, ip)
            }
        }

        @Nested
        inner class Json : JsonTest(
            filename = "event-data/source/user.json",
            expectedValue = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            ),
            deserializeAs = typeOf<EventSourceData>()
        )
    }
}
