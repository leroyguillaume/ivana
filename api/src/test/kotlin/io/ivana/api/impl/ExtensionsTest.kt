@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.EventSource
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.util.*

internal class ExtensionsTest {
    internal class toDataTest {
        @Test
        fun system() {
            EventSource.System.toData() shouldBe EventSourceData.System
        }

        @Test
        fun user() {
            val id = UUID.randomUUID()
            val ip = InetAddress.getByName("127.0.0.1")
            EventSource.User(id, ip).toData() shouldBe EventSourceData.User(id, ip)
        }
    }
}
