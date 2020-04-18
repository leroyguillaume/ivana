@file:Suppress("ClassName")

package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.*
import io.ivana.api.impl.PhotoAlreadyUploadedException
import io.ivana.api.security.Permission
import io.ivana.api.web.AbstractControllerTest
import io.ivana.core.LinkedPhotos
import io.ivana.core.Page
import io.ivana.core.Photo
import io.ivana.core.Transform
import io.ivana.dto.ErrorDto
import io.ivana.dto.PhotoUploadResultsDto
import io.ivana.dto.TransformDto
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
internal class PhotoControllerTest : AbstractControllerTest() {
    @Nested
    inner class get {
        private val linkedPhotos = LinkedPhotos(
            current = Photo(
                id = UUID.randomUUID(),
                ownerId = userPrincipal.user.id,
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash1",
                no = 1
            ),
            previous = Photo(
                id = UUID.randomUUID(),
                ownerId = userPrincipal.user.id,
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash2",
                no = 2
            ),
            next = Photo(
                id = UUID.randomUUID(),
                ownerId = userPrincipal.user.id,
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash3",
                no = 3
            )
        )
        private val photoSimpleDto = linkedPhotos.current.toSimpleDto()
        private val photoNavigableDto = linkedPhotos.toNavigableDto()
        private val method = HttpMethod.GET
        private val uri = "$PhotoApiEndpoint/${linkedPhotos.current.id}"

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
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, linkedPhotos.current.id))
                .thenReturn(EnumSet.complementOf(EnumSet.of(Permission.Read)))
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, linkedPhotos.current.id)
        }

        @Test
        fun `should return 200 (simple)`() = authenticated {
            whenever(
                userPhotoAuthzRepo.fetch(
                    principal.user.id,
                    linkedPhotos.current.id
                )
            ).thenReturn(setOf(Permission.Read))
            whenever(photoService.getById(linkedPhotos.current.id)).thenReturn(linkedPhotos.current)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = photoSimpleDto
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, linkedPhotos.current.id)
            verify(photoService).getById(linkedPhotos.current.id)
        }

        @Test
        fun `should return 200 (navigable)`() = authenticated {
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, linkedPhotos.current.id))
                .thenReturn(setOf(Permission.Read))
            whenever(photoService.getLinkedById(linkedPhotos.current.id)).thenReturn(linkedPhotos)
            callAndExpectDto(
                method = method,
                uri = uri,
                params = mapOf(NavigableParamName to listOf("true")),
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = photoNavigableDto
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, linkedPhotos.current.id)
            verify(photoService).getLinkedById(linkedPhotos.current.id)
        }
    }

    @Nested
    inner class getAll {
        private val pageNo = 2
        private val pageSize = 3
        private val page = Page(
            content = listOf(
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = userPrincipal.user.id,
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash1",
                    no = 1
                ),
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = userPrincipal.user.id,
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash2",
                    no = 2
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )
        private val pageDto = page.toDto { it.toSimpleDto() }
        private val method = HttpMethod.GET
        private val uri = PhotoApiEndpoint

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
        fun `should return 400 if parameters are lower than 1`() = authenticated {
            callAndExpectDto(
                method = method,
                params = mapOf(
                    PageParamName to listOf("-1"),
                    SizeParamName to listOf("-1")
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(minErrorDto(PageParamName, 1), minErrorDto(SizeParamName, 1))
                )
            )
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(photoService.getAll(principal.user.id, pageNo, pageSize)).thenReturn(page)
            callAndExpectDto(
                method = method,
                params = mapOf(
                    PageParamName to listOf(pageNo.toString()),
                    SizeParamName to listOf(pageSize.toString())
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = pageDto
            )
            verify(photoService).getAll(principal.user.id, pageNo, pageSize)
        }
    }

    @Nested
    inner class getCompressedFile {
        private val jpgPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = userPrincipal.user.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash1",
            no = 1
        )
        private val jpgFile = File(javaClass.getResource("/data/photo.jpg").file)
        private val pngPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = userPrincipal.user.id,
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
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, jpgPhoto.id))
                .thenReturn(EnumSet.complementOf(EnumSet.of(Permission.Read)))
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
            ownerId = userPrincipal.user.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash1",
            no = 1
        )
        private val jpgFile = File(javaClass.getResource("/data/photo.jpg").file)
        private val pngPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = userPrincipal.user.id,
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
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, jpgPhoto.id))
                .thenReturn(EnumSet.complementOf(EnumSet.of(Permission.Read)))
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
    inner class transform {
        private val id = UUID.randomUUID()
        private val method = HttpMethod.PUT
        private val uri = "$PhotoApiEndpoint/$id$TransformPhotoEndpoint"

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
            val transformDto = TransformDto.Rotation(TransformDto.Rotation.Direction.Clockwise)
            whenever(userPhotoAuthzRepo.fetch(principal.user.id, id))
                .thenReturn(EnumSet.complementOf(EnumSet.of(Permission.Update)))
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(transformDto),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userPhotoAuthzRepo).fetch(principal.user.id, id)
        }

        @Nested
        inner class rotation {
            private val transformDto = TransformDto.Rotation(TransformDto.Rotation.Direction.Clockwise)
            private val transform = Transform.Rotation(Transform.Rotation.Direction.Clockwise)

            @Test
            fun `should return 204`() = authenticated {
                whenever(userPhotoAuthzRepo.fetch(principal.user.id, id)).thenReturn(setOf(Permission.Update))
                callAndExpectDto(
                    method = method,
                    uri = uri,
                    reqCookies = listOf(accessTokenCookie()),
                    reqContent = mapper.writeValueAsString(transformDto),
                    status = HttpStatus.NO_CONTENT
                )
                verify(userPhotoAuthzRepo).fetch(principal.user.id, id)
                verify(photoService).transform(id, transform, source)
            }
        }
    }

    @Nested
    inner class upload {
        private val jpgFile = File(javaClass.getResource("/data/photo.jpg").file)
        private val pngFile = File(javaClass.getResource("/data/photo.png").file)
        private val gifFile = File(javaClass.getResource("/data/photo.gif").file)
        private val jpgPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = userPrincipal.user.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1
        )
        private val pngPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = userPrincipal.user.id,
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
                        PhotoUploadResultsDto.Result.Success(jpgPhoto.toSimpleDto()),
                        PhotoUploadResultsDto.Result.Success(pngPhoto.toSimpleDto()),
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
