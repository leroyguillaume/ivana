@file:Suppress("ClassName")

package io.ivana.api.impl

import org.junit.jupiter.api.Nested
import java.net.InetAddress
import java.util.*

internal class PhotoEventDataTest {
    @Nested
    inner class Deletion : JsonTest(
        filename = "event-data/photo/deletion.json",
        expectedValue = PhotoEventData.Deletion(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            )
        ),
        deserializeAs = typeOf<PhotoEventData.Deletion>()
    )

    @Nested
    inner class Transform {
        @Nested
        inner class Rotation {
            @Nested
            inner class clockwise : JsonTest(
                filename = "event-data/photo/transform/rotation/clockwise.json",
                expectedValue = PhotoEventData.Transform(
                    source = EventSourceData.User(
                        id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                        ip = InetAddress.getByName("127.0.0.1")
                    ),
                    content = PhotoEventData.Transform.Content.Rotation(
                        PhotoEventData.Transform.Content.Rotation.Direction.Clockwise
                    )
                ),
                deserializeAs = typeOf<PhotoEventData.Transform>()
            )

            @Nested
            inner class counterclockwise : JsonTest(
                filename = "event-data/photo/transform/rotation/counterclockwise.json",
                expectedValue = PhotoEventData.Transform(
                    source = EventSourceData.User(
                        id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                        ip = InetAddress.getByName("127.0.0.1")
                    ),
                    content = PhotoEventData.Transform.Content.Rotation(
                        PhotoEventData.Transform.Content.Rotation.Direction.Counterclockwise
                    )
                ),
                deserializeAs = typeOf<PhotoEventData.Transform>()
            )
        }
    }

    @Nested
    inner class Upload : JsonTest(
        filename = "event-data/photo/upload.json",
        expectedValue = PhotoEventData.Upload(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            ),
            content = PhotoEventData.Upload.Content(
                type = PhotoTypeData.Jpg,
                hash = "hash"
            )
        ),
        deserializeAs = typeOf<PhotoEventData.Upload>()
    )
}
