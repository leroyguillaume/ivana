@file:Suppress("ClassName")

package io.ivana.api.impl

import org.junit.jupiter.api.Nested
import java.net.InetAddress
import java.util.*

internal class AlbumEventDataTest {
    @Nested
    inner class Creation : JsonTest(
        filename = "event-data/album/creation.json",
        expectedValue = AlbumEventData.Creation(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            ),
            content = AlbumEventData.Creation.Content("album")
        ),
        deserializeAs = typeOf<AlbumEventData.Creation>()
    )
}
