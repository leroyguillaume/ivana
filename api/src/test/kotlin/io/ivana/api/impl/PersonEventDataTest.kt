@file:Suppress("ClassName")

package io.ivana.api.impl

import org.junit.jupiter.api.Nested
import java.net.InetAddress
import java.util.*

internal class PersonEventDataTest {
    @Nested
    inner class Creation : JsonTest(
        filename = "event-data/person/creation.json",
        expectedValue = PersonEventData.Creation(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            ),
            content = PersonEventData.Creation.Content(
                lastName = "Leroy",
                firstName = "Guillaume"
            )
        ),
        deserializeAs = typeOf<PersonEventData.Creation>()
    )

    @Nested
    inner class Deletion : JsonTest(
        filename = "event-data/person/deletion.json",
        expectedValue = PersonEventData.Deletion(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            )
        ),
        deserializeAs = typeOf<PersonEventData.Deletion>()
    )

    @Nested
    inner class Update : JsonTest(
        filename = "event-data/person/update.json",
        expectedValue = PersonEventData.Update(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            ),
            content = PersonEventData.Update.Content(
                lastName = "Leroy",
                firstName = "Guillaume"
            )
        ),
        deserializeAs = typeOf<PersonEventData.Update>()
    )
}
