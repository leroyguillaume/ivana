@file:Suppress("ClassName")

package io.ivana.api.web.v1

import io.ivana.core.Photo
import io.ivana.core.PhotosTimeWindow
import io.ivana.dto.NavigablePhotoDto
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
            rawUri = rawUri(photo.id),
            compressedUri = compressedUri(photo.id)
        )
        photo.toDto() shouldBe dto
    }

    @Test
    fun photosTimeWindowToNavigablePhotoDto() {
        val photosTimeWindow = PhotosTimeWindow(
            current = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash2",
                no = 2
            ),
            previous = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash1",
                no = 1
            ),
            next = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 3
            )
        )
        val dto = NavigablePhotoDto(
            id = photosTimeWindow.current.id,
            rawUri = rawUri(photosTimeWindow.current.id),
            compressedUri = compressedUri(photosTimeWindow.current.id),
            previous = PhotoDto(
                id = photosTimeWindow.previous!!.id,
                rawUri = rawUri(photosTimeWindow.previous!!.id),
                compressedUri = compressedUri(photosTimeWindow.previous!!.id)
            ),
            next = PhotoDto(
                id = photosTimeWindow.next!!.id,
                rawUri = rawUri(photosTimeWindow.next!!.id),
                compressedUri = compressedUri(photosTimeWindow.next!!.id)
            )
        )
        photosTimeWindow.toDto() shouldBe dto
    }

    private fun rawUri(id: UUID) = URI("$PhotoApiEndpoint/$id$RawPhotoEndpoint")

    private fun compressedUri(id: UUID) = URI("$PhotoApiEndpoint/$id$CompressedPhotoEndpoint")
}
