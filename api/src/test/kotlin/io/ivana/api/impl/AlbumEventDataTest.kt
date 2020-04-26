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

    @Nested
    inner class Deletion : JsonTest(
        filename = "event-data/album/deletion.json",
        expectedValue = AlbumEventData.Deletion(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            )
        ),
        deserializeAs = typeOf<AlbumEventData.Deletion>()
    )

    @Nested
    inner class Update : JsonTest(
        filename = "event-data/album/update.json",
        expectedValue = AlbumEventData.Update(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            ),
            content = AlbumEventData.Update.Content(
                name = "album",
                photosToAdd = listOf(UUID.fromString("c33d6e4a-6c55-4ed8-a23a-0c36dae1cc91")),
                photosToRemove = listOf(UUID.fromString("1890c0d1-c5db-4563-b43c-7401807e0353"))
            )
        ),
        deserializeAs = typeOf<AlbumEventData.Update>()
    )
}
