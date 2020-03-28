@file:Suppress("ClassName")

package io.ivana.api.impl

import org.junit.jupiter.api.Nested
import java.net.InetAddress
import java.util.*

internal class UserEventDataTest {
    @Nested
    inner class Creation : JsonTest(
        filename = "event-data/user/creation.json",
        expectedValue = UserEventData.Creation(
            EventSourceData.System,
            UserEventData.Creation.Content(
                name = "admin",
                hashedPwd = "foo123"
            )
        ),
        deserializeAs = typeOf<UserEventData.Creation>()
    )

    @Nested
    inner class Login : JsonTest(
        filename = "event-data/user/login.json",
        expectedValue = UserEventData.Login(
            EventSourceData.User(
                UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                InetAddress.getByName("127.0.0.1")
            )
        ),
        deserializeAs = typeOf<UserEventData.Login>()
    )
}
