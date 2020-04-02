@file:Suppress("ClassName")

package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.*
import io.ivana.api.impl.PhotoAlreadyUploadedException
import io.ivana.api.security.Permission
import io.ivana.core.EventSource
import io.ivana.core.Photo
import io.ivana.dto.ErrorDto
import io.ivana.dto.PhotoDto
import io.ivana.dto.PhotoUploadResultsDto
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.net.InetAddress
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
internal class PhotoControllerTest : AbstractControllerTest() {
    @Nested
    inner class get {
        private val photo = Photo(
            id = UUID.randomUUID(),
            ownerId = principal.user.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1
        )
        private val photoDto = photo.toDto()

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpect(
                method = HttpMethod.GET,
                uri = "$PhotoApiEndpoint/${photo.id}",
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 403 if user does not have permission`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, photo.id)).thenReturn(emptySet())
            callAndExpect(
                method = HttpMethod.GET,
                uri = "$PhotoApiEndpoint/${photo.id}",
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, photo.id)
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, photo.id)).thenReturn(setOf(Permission.Read))
            whenever(photoService.getById(photo.id)).thenReturn(photo)
            callAndExpect(
                method = HttpMethod.GET,
                uri = "$PhotoApiEndpoint/${photo.id}",
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = photoDto
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, photo.id)
            verify(photoService).getById(photo.id)
        }
    }

    @Nested
    inner class upload {
        private val jpgFile = File(javaClass.getResource("/data/photo.jpg").file)
        private val pngFile = File(javaClass.getResource("/data/photo.png").file)
        private val gifFile = File(javaClass.getResource("/data/photo.gif").file)
        private val source = EventSource.User(
            id = principal.user.id,
            ip = InetAddress.getByName("127.0.0.1")
        )
        private val jpgPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = principal.user.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1
        )
        private val pngPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = principal.user.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Png,
            hash = "hash",
            no = 2
        )

        @Test
        fun `should return 201`() = authenticated {
            var callNb = 0
            whenever(photoService.uploadPhoto(any(), any(), eq(source))).then {
                when (++callNb) {
                    1 -> jpgPhoto
                    2 -> pngPhoto
                    3 -> throw PhotoAlreadyUploadedException(jpgPhoto)
                    4 -> throw RuntimeException()
                    else -> throw IllegalStateException("Too many calls")
                }
            }
            callAndExpect(
                uri = PhotoApiEndpoint,
                reqCookies = listOf(accessTokenCookie()),
                files = listOf(
                    // JPG upload
                    MockMultipartFile(FilesParamName, "image.jpg", MediaType.IMAGE_JPEG_VALUE, jpgFile.inputStream()),
                    // PNG upload
                    MockMultipartFile(FilesParamName, "image.png", MediaType.IMAGE_JPEG_VALUE, pngFile.inputStream()),
                    // Without content type
                    MockMultipartFile(FilesParamName, jpgFile.inputStream()),
                    // Unsupported content type
                    MockMultipartFile(FilesParamName, "image.gif", MediaType.IMAGE_GIF_VALUE, gifFile.inputStream()),
                    // Duplicate resource
                    MockMultipartFile(FilesParamName, "image.jpg", MediaType.IMAGE_JPEG_VALUE, jpgFile.inputStream()),
                    // Internal error
                    MockMultipartFile(FilesParamName, "image.jpg", MediaType.IMAGE_JPEG_VALUE, jpgFile.inputStream())
                ),
                status = HttpStatus.CREATED,
                respDto = PhotoUploadResultsDto(
                    listOf(
                        PhotoUploadResultsDto.Result.Success(jpgPhoto.toDto()),
                        PhotoUploadResultsDto.Result.Success(pngPhoto.toDto()),
                        PhotoUploadResultsDto.Result.Failure(
                            ErrorDto.UnsupportedMediaType(PhotoController.MediaTypeToPhotoType.keys)
                        ),
                        PhotoUploadResultsDto.Result.Failure(
                            ErrorDto.UnsupportedMediaType(PhotoController.MediaTypeToPhotoType.keys)
                        ),
                        PhotoUploadResultsDto.Result.Failure(
                            ErrorDto.DuplicateResource(URI("$PhotoApiEndpoint/${jpgPhoto.id}"))
                        ),
                        PhotoUploadResultsDto.Result.Failure(ErrorDto.InternalError)
                    )
                )
            )
            verify(photoService, times(4)).uploadPhoto(any(), any(), eq(source))
        }
    }

    private fun Photo.toDto() = PhotoDto(
        id = id,
        rawUri = URI("$PhotoApiEndpoint/$id$RawPhotoEndpoint"),
        compressedUri = URI("$PhotoApiEndpoint/$id$CompressedPhotoEndpoint"),
        no = no
    )
}
