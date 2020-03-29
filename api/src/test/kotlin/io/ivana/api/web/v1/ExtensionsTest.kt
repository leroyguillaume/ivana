@file:Suppress("ClassName")

package io.ivana.api.web.v1

import io.ivana.core.Photo
import io.ivana.dto.PhotoDto
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

internal class ExtensionsTest {
    @Test
    fun photoToDtoTest() {
        val photo = Photo(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1
        )
        val dto = PhotoDto(
            id = photo.id,
            rawUri = URI("$PhotoApiEndpoint/${photo.id}$RawPhotoEndpoint"),
            compressedUri = URI("$PhotoApiEndpoint/${photo.id}$CompressedPhotoEndpoint"),
            no = 1
        )
        photo.toDto() shouldBe dto
    }
}
