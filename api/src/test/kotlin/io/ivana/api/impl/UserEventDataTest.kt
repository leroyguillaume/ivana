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
                hashedPwd = "hashedPwd",
                role = RoleData.SuperAdmin
            )
        ),
        deserializeAs = typeOf<UserEventData.Creation>()
    )

    @Nested
    inner class Deletion : JsonTest(
        filename = "event-data/user/deletion.json",
        expectedValue = UserEventData.Deletion(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            )
        ),
        deserializeAs = typeOf<UserEventData.Deletion>()
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

    @Nested
    inner class PasswordUpdate : JsonTest(
        filename = "event-data/user/password-update.json",
        expectedValue = UserEventData.PasswordUpdate(
            EventSourceData.System,
            UserEventData.PasswordUpdate.Content(
                newHashedPwd = "newHashedPwd"
            )
        ),
        deserializeAs = typeOf<UserEventData.PasswordUpdate>()
    )
}
