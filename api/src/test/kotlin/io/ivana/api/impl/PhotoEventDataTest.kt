@file:Suppress("ClassName")

package io.ivana.api.impl

import org.junit.jupiter.api.Nested
import java.net.InetAddress
import java.util.*

internal class PhotoEventDataTest {
    @Nested
    inner class Upload : JsonTest(
        filename = "event-data/photo/upload.json",
        expectedValue = PhotoEventData.Upload(
            EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            ),
            PhotoEventData.Upload.Content(
                type = PhotoTypeData.Jpg,
                hash = "hash"
            )
        ),
        deserializeAs = typeOf<PhotoEventData.Upload>()
    )
}
