@file:Suppress("ClassName")

package io.ivana.api.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.net.InetAddress
import java.time.LocalDate
import java.util.*

@SpringBootTest
internal class PhotoEventDataTest {
    @Autowired
    private lateinit var mapper: ObjectMapper

    @Nested
    inner class Deletion : JsonTest(
        filename = "event-data/photo/deletion.json",
        expectedValue = PhotoEventData.Deletion(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            )
        ),
        deserializeAs = typeOf<PhotoEventData.Deletion>(),
        mapper = mapper
    )

    @Nested
    inner class Transform {
        @Nested
        inner class Rotation : JsonTest(
            filename = "event-data/photo/transform/rotation.json",
            expectedValue = PhotoEventData.Transform(
                source = EventSourceData.User(
                    id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                    ip = InetAddress.getByName("127.0.0.1")
                ),
                content = PhotoEventData.Transform.Content.Rotation(90.0)
            ),
            deserializeAs = typeOf<PhotoEventData.Transform>(),
            mapper = mapper
        )
    }

    @Nested
    inner class Update {
        @Nested
        inner class Default : JsonTest(
            filename = "event-data/photo/update_default.json",
            expectedValue = PhotoEventData.Update(
                source = EventSourceData.User(
                    id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                    ip = InetAddress.getByName("127.0.0.1")
                ),
                content = PhotoEventData.Update.Content()
            ),
            deserializeAs = typeOf<PhotoEventData.Update>(),
            mapper = mapper
        )

        @Nested
        inner class Complete : JsonTest(
            filename = "event-data/photo/update_complete.json",
            expectedValue = PhotoEventData.Update(
                source = EventSourceData.User(
                    id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                    ip = InetAddress.getByName("127.0.0.1")
                ),
                content = PhotoEventData.Update.Content(
                    shootingDate = LocalDate.parse("2020-06-07")
                )
            ),
            deserializeAs = typeOf<PhotoEventData.Update>(),
            mapper = mapper
        )
    }

    @Nested
    inner class UpdatePeople : JsonTest(
        filename = "event-data/photo/update-people.json",
        expectedValue = PhotoEventData.UpdatePeople(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            ),
            content = PhotoEventData.UpdatePeople.Content(
                peopleToAdd = setOf(UUID.fromString("a526a3f3-98dc-4cca-8d5b-43a20fe02963")),
                peopleToRemove = setOf(UUID.fromString("faf7a32c-8211-4536-9a18-417a34aedf21"))
            )
        ),
        deserializeAs = typeOf<PhotoEventData.UpdatePeople>(),
        mapper = mapper
    )

    @Nested
    inner class UpdatePermissions : JsonTest(
        filename = "event-data/photo/update-permissions.json",
        expectedValue = PhotoEventData.UpdatePermissions(
            source = EventSourceData.User(
                id = UUID.fromString("644465bf-a2d5-43aa-b79d-84b9aa543bad"),
                ip = InetAddress.getByName("127.0.0.1")
            ),
            content = PhotoEventData.UpdatePermissions.Content(
                permissionsToAdd = setOf(
                    SubjectPermissionsData(
                        subjectId = UUID.fromString("a526a3f3-98dc-4cca-8d5b-43a20fe02963"),
                        permissions = setOf(PermissionData.Read)
                    )
                ),
                permissionsToRemove = setOf(
                    SubjectPermissionsData(
                        subjectId = UUID.fromString("faf7a32c-8211-4536-9a18-417a34aedf21"),
                        permissions = setOf(PermissionData.Delete)
                    )
                )
            )
        ),
        deserializeAs = typeOf<PhotoEventData.UpdatePermissions>(),
        mapper = mapper
    )

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
        deserializeAs = typeOf<PhotoEventData.Upload>(),
        mapper = mapper
    )
}
