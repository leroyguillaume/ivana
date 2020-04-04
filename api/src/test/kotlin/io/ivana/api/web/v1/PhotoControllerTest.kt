@file:Suppress("ClassName")

package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.*
import io.ivana.api.impl.PhotoAlreadyUploadedException
import io.ivana.api.security.Permission
import io.ivana.core.EventSource
import io.ivana.core.Photo
import io.ivana.dto.ErrorDto
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
        private val method = HttpMethod.GET
        private val uri = "$PhotoApiEndpoint/${photo.id}"

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 403 if user does not have permission`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, photo.id)).thenReturn(emptySet())
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, photo.id)
        }

        @Test
        fun `should return 400 if navigable is not boolean`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                params = mapOf(NavigableParamName to listOf("a")),
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.InvalidParameter(
                    parameter = NavigableParamName,
                    reason = "must be boolean"
                )
            )
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, photo.id)).thenReturn(setOf(Permission.Read))
            whenever(photoService.getById(photo.id)).thenReturn(photo)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = photoDto
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, photo.id)
            verify(photoService).getById(photo.id)
        }
    }

    @Nested
    inner class getCompressedFile {
        private val jpgPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = principal.user.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash1",
            no = 1
        )
        private val jpgFile = File(javaClass.getResource("/data/photo.jpg").file)
        private val pngPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = principal.user.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Png,
            hash = "hash2",
            no = 2
        )
        private val pngFile = File(javaClass.getResource("/data/photo.png").file)
        private val method = HttpMethod.GET
        private val jpgUri = "$PhotoApiEndpoint/${jpgPhoto.id}$CompressedPhotoEndpoint"
        private val pngUri = "$PhotoApiEndpoint/${pngPhoto.id}$CompressedPhotoEndpoint"

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = jpgUri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 403 if user does not have permission`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, jpgPhoto.id)).thenReturn(emptySet())
            callAndExpectDto(
                method = method,
                uri = jpgUri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, jpgPhoto.id)
        }

        @Test
        fun `should return 200 (jpg)`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, jpgPhoto.id)).thenReturn(setOf(Permission.Read))
            whenever(photoService.getById(jpgPhoto.id)).thenReturn(jpgPhoto)
            whenever(photoService.getCompressedFile(jpgPhoto)).thenReturn(jpgFile)
            callAndExpectFile(
                method = method,
                uri = jpgUri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                expectedContentType = MediaType.IMAGE_JPEG,
                expectedFile = jpgFile
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, jpgPhoto.id)
            verify(photoService).getById(jpgPhoto.id)
            verify(photoService).getCompressedFile(jpgPhoto)
        }

        @Test
        fun `should return 200 (png)`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, pngPhoto.id)).thenReturn(setOf(Permission.Read))
            whenever(photoService.getById(pngPhoto.id)).thenReturn(pngPhoto)
            whenever(photoService.getCompressedFile(pngPhoto)).thenReturn(pngFile)
            callAndExpectFile(
                method = method,
                uri = pngUri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                expectedContentType = MediaType.IMAGE_PNG,
                expectedFile = pngFile
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, pngPhoto.id)
            verify(photoService).getById(pngPhoto.id)
            verify(photoService).getCompressedFile(pngPhoto)
        }
    }

    @Nested
    inner class getRawFile {
        private val jpgPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = principal.user.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash1",
            no = 1
        )
        private val jpgFile = File(javaClass.getResource("/data/photo.jpg").file)
        private val pngPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = principal.user.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Png,
            hash = "hash2",
            no = 2
        )
        private val pngFile = File(javaClass.getResource("/data/photo.png").file)
        private val method = HttpMethod.GET
        private val jpgUri = "$PhotoApiEndpoint/${jpgPhoto.id}$RawPhotoEndpoint"
        private val pngUri = "$PhotoApiEndpoint/${pngPhoto.id}$RawPhotoEndpoint"

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = jpgUri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 403 if user does not have permission`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, jpgPhoto.id)).thenReturn(emptySet())
            callAndExpectDto(
                method = method,
                uri = jpgUri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, jpgPhoto.id)
        }

        @Test
        fun `should return 200 (jpg)`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, jpgPhoto.id)).thenReturn(setOf(Permission.Read))
            whenever(photoService.getById(jpgPhoto.id)).thenReturn(jpgPhoto)
            whenever(photoService.getRawFile(jpgPhoto)).thenReturn(jpgFile)
            callAndExpectFile(
                method = method,
                uri = jpgUri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                expectedContentType = MediaType.IMAGE_JPEG,
                expectedFile = jpgFile
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, jpgPhoto.id)
            verify(photoService).getById(jpgPhoto.id)
            verify(photoService).getRawFile(jpgPhoto)
        }

        @Test
        fun `should return 200 (png)`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, pngPhoto.id)).thenReturn(setOf(Permission.Read))
            whenever(photoService.getById(pngPhoto.id)).thenReturn(pngPhoto)
            whenever(photoService.getRawFile(pngPhoto)).thenReturn(pngFile)
            callAndExpectFile(
                method = method,
                uri = pngUri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                expectedContentType = MediaType.IMAGE_PNG,
                expectedFile = pngFile
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, pngPhoto.id)
            verify(photoService).getById(pngPhoto.id)
            verify(photoService).getRawFile(pngPhoto)
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
        private val uri = PhotoApiEndpoint

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
            multipartCallAndExpectDto(
                uri = uri,
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
}
