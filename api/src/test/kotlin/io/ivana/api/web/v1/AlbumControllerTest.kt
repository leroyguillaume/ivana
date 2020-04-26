@file:Suppress("ClassName")

package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ivana.api.impl.AlbumAlreadyContainsPhotosException
import io.ivana.api.impl.PhotosNotFoundException
import io.ivana.api.security.Permission
import io.ivana.api.web.AbstractControllerTest
import io.ivana.core.Album
import io.ivana.core.AlbumEvent
import io.ivana.core.Page
import io.ivana.core.Photo
import io.ivana.dto.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
internal class AlbumControllerTest : AbstractControllerTest() {
    @Nested
    inner class create {
        private val method = HttpMethod.POST
        private val uri = AlbumApiEndpoint
        private val creationDto = AlbumCreationDto("album")
        private val album = Album(
            id = UUID.randomUUID(),
            ownerId = userPrincipal.user.id,
            name = "album",
            creationDate = OffsetDateTime.now()
        )
        private val albumDto = album.toDto()

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
        fun `should return 400 if params are too short`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(creationDto.copy("a")),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(listOf(sizeErrorDto("name", AlbumNameMinSize, AlbumNameMaxSize)))
            )
        }

        @Test
        fun `should return 400 if params are too long`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(
                    creationDto.copy(name = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                ),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(listOf(sizeErrorDto("name", AlbumNameMinSize, AlbumNameMaxSize)))
            )
        }

        @Test
        fun `should return 201`() = authenticated {
            whenever(albumService.create(album.name, source)).thenReturn(album)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(creationDto),
                status = HttpStatus.CREATED,
                respDto = albumDto
            )
            verify(albumService).create(album.name, source)
        }
    }

    @Nested
    inner class get {
        private val album = Album(
            id = UUID.randomUUID(),
            ownerId = userPrincipal.user.id,
            name = "album",
            creationDate = OffsetDateTime.now()
        )
        private val albumDto = album.toDto()
        private val method = HttpMethod.GET
        private val uri = "$AlbumApiEndpoint/${album.id}"

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
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id))
                .thenReturn(EnumSet.complementOf(EnumSet.of(Permission.Read)))
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id)).thenReturn(setOf(Permission.Read))
            whenever(albumService.getById(album.id)).thenReturn(album)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = albumDto
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
            verify(albumService).getById(album.id)
        }
    }

    @Nested
    inner class getAll {
        private val pageNo = 2
        private val pageSize = 3
        private val page = Page(
            content = listOf(
                Album(
                    id = UUID.randomUUID(),
                    ownerId = userPrincipal.user.id,
                    name = "album1",
                    creationDate = OffsetDateTime.now()
                ),
                Album(
                    id = UUID.randomUUID(),
                    ownerId = userPrincipal.user.id,
                    name = "album2",
                    creationDate = OffsetDateTime.now()
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )
        private val pageDto = page.toDto { it.toDto() }
        private val method = HttpMethod.GET
        private val uri = AlbumApiEndpoint

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
            whenever(albumService.getAll(principal.user.id, pageNo, pageSize)).thenReturn(page)
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
            verify(albumService).getAll(principal.user.id, pageNo, pageSize)
        }
    }

    @Nested
    inner class getAllPhotos {
        private val albumId = UUID.randomUUID()
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
        private val uri = "$AlbumApiEndpoint/$albumId$ContentEndpoint"

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
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, albumId)).thenReturn(setOf(Permission.Read))
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
            verify(userAlbumAuthzRepo).fetch(principal.user.id, albumId)
        }

        @Test
        fun `should return 403 if user does not have permission`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, albumId))
                .thenReturn(EnumSet.complementOf(EnumSet.of(Permission.Read)))
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, albumId)
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, albumId)).thenReturn(setOf(Permission.Read))
            whenever(albumService.getAllPhotos(albumId, pageNo, pageSize)).thenReturn(page)
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
            verify(userAlbumAuthzRepo).fetch(principal.user.id, albumId)
            verify(albumService).getAllPhotos(albumId, pageNo, pageSize)
        }
    }

    @Nested
    inner class update {
        private val updateDto = AlbumUpdateDto(
            name = "album",
            photosToAdd = UUID.randomUUID().let { id -> listOf(id, id) },
            photosToRemove = UUID.randomUUID().let { id -> listOf(id, id) }
        )
        private val duplicateIds = updateDto.photosToAdd.toSet()
        private val updateContent = AlbumEvent.Update.Content(
            name = updateDto.name,
            photosToAdd = updateDto.photosToAdd.distinct(),
            photosToRemove = updateDto.photosToRemove.distinct()
        )
        private val album = Album(
            id = UUID.randomUUID(),
            ownerId = userPrincipal.user.id,
            name = "album",
            creationDate = OffsetDateTime.now()
        )
        private val albumDto = album.toDto()
        private val method = HttpMethod.PUT
        private val uri = "$AlbumApiEndpoint/${album.id}"

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
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id))
                .thenReturn(EnumSet.complementOf(EnumSet.of(Permission.Update)))
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(updateDto),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
        }

        @Test
        fun `should return 400 if params are too short`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(updateDto.copy("a")),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(listOf(sizeErrorDto("name", AlbumNameMinSize, AlbumNameMaxSize)))
            )
        }

        @Test
        fun `should return 400 if params are too long`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(
                    updateDto.copy(name = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                ),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(listOf(sizeErrorDto("name", AlbumNameMinSize, AlbumNameMaxSize)))
            )
        }

        @Test
        fun `should return 400 if photos does not exist`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id)).thenReturn(setOf(Permission.Update))
            whenever(albumService.update(album.id, updateContent, source)).thenAnswer {
                throw PhotosNotFoundException(duplicateIds)
            }
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(updateDto),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.PhotosNotFound(duplicateIds)
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
            verify(albumService).update(album.id, updateContent, source)
        }

        @Test
        fun `should return 409 if album already contains photos`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id)).thenReturn(setOf(Permission.Update))
            whenever(albumService.update(album.id, updateContent, source)).thenAnswer {
                throw AlbumAlreadyContainsPhotosException(duplicateIds)
            }
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(updateDto),
                status = HttpStatus.CONFLICT,
                respDto = ErrorDto.AlbumAlreadyContainsPhotos(duplicateIds)
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
            verify(albumService).update(album.id, updateContent, source)
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id)).thenReturn(setOf(Permission.Update))
            whenever(albumService.update(album.id, updateContent, source)).thenReturn(album)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(updateDto),
                status = HttpStatus.OK,
                respDto = albumDto
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
            verify(albumService).update(album.id, updateContent, source)
        }
    }
}
