@file:Suppress("ClassName")

package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ivana.api.impl.AlbumAlreadyContainsPhotosException
import io.ivana.api.impl.OwnerPermissionsUpdateException
import io.ivana.api.web.AbstractControllerTest
import io.ivana.core.*
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
        private val albumDto = album.toLightDto()

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
    inner class delete {
        private val albumId = UUID.randomUUID()
        private val method = HttpMethod.DELETE
        private val uri = "$AlbumApiEndpoint/$albumId"

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
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, albumId))
                .thenReturn(EnumSet.complementOf(EnumSet.of(Permission.Delete)))
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
        fun `should return 204`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, albumId)).thenReturn(setOf(Permission.Delete))
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.NO_CONTENT
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, albumId)
            verify(albumService).delete(albumId, source)
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
        private val permissions = setOf(Permission.Read)
        private val albumDto = album.toCompleteDto(permissions)
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
            whenever(albumService.getPermissions(album.id, principal.user.id)).thenReturn(permissions)
            whenever(albumService.getById(album.id)).thenReturn(album)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = albumDto
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
            verify(albumService).getPermissions(album.id, principal.user.id)
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
        private val pageDto = page.toDto { it.toLightDto() }
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
                    no = 1,
                    version = 1
                ),
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = userPrincipal.user.id,
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash2",
                    no = 2,
                    version = 1
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )
        private val pageDto = page.toDto { it.toLightDto() }
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
            whenever(albumService.getAllPhotos(albumId, principal.user.id, pageNo, pageSize)).thenReturn(page)
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
            verify(albumService).getAllPhotos(albumId, principal.user.id, pageNo, pageSize)
        }
    }

    @Nested
    inner class getPermissions {
        private val albumId = UUID.randomUUID()
        private val pageNo = 2
        private val pageSize = 3
        private val page = Page(
            content = listOf(
                SubjectPermissions(
                    subjectId = userPrincipal.user.id,
                    permissions = setOf(Permission.Read)
                ),
                SubjectPermissions(
                    subjectId = adminPrincipal.user.id,
                    permissions = setOf(Permission.Delete)
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )
        private val users = setOf(userPrincipal.user, adminPrincipal.user)
        private val usersIds = users.map { it.id }.toSet()
        private val pageDto = PageDto(
            content = listOf(
                SubjectPermissionsDto(
                    subjectId = userPrincipal.user.id,
                    subjectName = userPrincipal.user.name,
                    permissions = setOf(PermissionDto.Read)
                ),
                SubjectPermissionsDto(
                    subjectId = adminPrincipal.user.id,
                    subjectName = adminPrincipal.user.name,
                    permissions = setOf(PermissionDto.Delete)
                )
            ),
            no = page.no,
            totalItems = page.totalItems,
            totalPages = page.totalPages
        )
        private val method = HttpMethod.GET
        private val uri = "$AlbumApiEndpoint/$albumId$PermissionsEndpoint"

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
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, albumId))
                .thenReturn(setOf(Permission.UpdatePermissions))
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
                .thenReturn(EnumSet.complementOf(EnumSet.of(Permission.UpdatePermissions)))
            callAndExpectDto(
                method = method,
                params = mapOf(
                    PageParamName to listOf(pageNo.toString()),
                    SizeParamName to listOf(pageSize.toString())
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, albumId)
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, albumId))
                .thenReturn(setOf(Permission.UpdatePermissions))
            whenever(albumService.getAllPermissions(albumId, pageNo, pageSize)).thenReturn(page)
            whenever(userService.getAllByIds(usersIds)).thenReturn(users)
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
            verify(albumService).getAllPermissions(albumId, pageNo, pageSize)
            verify(userService).getAllByIds(usersIds)
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
        private val albumDto = album.toLightDto()
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
        fun `should return 403 if user does not have permission on photo`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id)).thenReturn(setOf(Permission.Update))
            whenever(photoService.userCanReadAll(updateDto.photosToAdd.toSet(), principal.user.id)).thenReturn(false)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(updateDto),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
            verify(photoService).userCanReadAll(updateDto.photosToAdd.toSet(), principal.user.id)
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
        fun `should return 409 if album already contains photos`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id)).thenReturn(setOf(Permission.Update))
            whenever(photoService.userCanReadAll(updateDto.photosToAdd.toSet(), principal.user.id)).thenReturn(true)
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
            verify(photoService).userCanReadAll(updateDto.photosToAdd.toSet(), principal.user.id)
            verify(albumService).update(album.id, updateContent, source)
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id)).thenReturn(setOf(Permission.Update))
            whenever(photoService.userCanReadAll(updateDto.photosToAdd.toSet(), principal.user.id)).thenReturn(true)
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
            verify(photoService).userCanReadAll(updateDto.photosToAdd.toSet(), principal.user.id)
            verify(albumService).update(album.id, updateContent, source)
        }
    }

    @Nested
    inner class updatePermissions {
        private val permissionToAdd = SubjectPermissionsUpdateDto(
            subjectId = userPrincipal.user.id,
            permissions = setOf(PermissionDto.Read)
        )
        private val permissionToRemove = SubjectPermissionsUpdateDto(
            subjectId = adminPrincipal.user.id,
            permissions = setOf(PermissionDto.Delete)
        )
        private val users = setOf(userPrincipal.user, adminPrincipal.user)
        private val usersIds = users.map { it.id }.toSet()
        private val dto = UpdatePermissionsDto(
            permissionsToAdd = setOf(permissionToAdd),
            permissionsToRemove = setOf(permissionToRemove)
        )
        private val permissionsToAdd = setOf(
            UserPermissions(
                user = userPrincipal.user,
                permissions = setOf(Permission.Read)
            )
        )
        private val permissionsToRemove = setOf(
            UserPermissions(
                user = adminPrincipal.user,
                permissions = setOf(Permission.Delete)
            )
        )
        private val album = Album(
            id = UUID.randomUUID(),
            name = "test",
            ownerId = userPrincipal.user.id,
            creationDate = OffsetDateTime.now()
        )
        private val method = HttpMethod.PUT
        private val uri = "$AlbumApiEndpoint/${album.id}$PermissionsEndpoint"

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
                reqContent = mapper.writeValueAsString(dto),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
        }

        @Test
        fun `should return 400 if owner permissions are update`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id)).thenReturn(setOf(Permission.Update))
            whenever(userService.getAllByIds(usersIds)).thenReturn(users)
            whenever(albumService.updatePermissions(album.id, permissionsToAdd, permissionsToRemove, source))
                .thenAnswer { throw OwnerPermissionsUpdateException() }
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(dto),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.OwnerPermissionsUpdate
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
            verify(userService).getAllByIds(usersIds)
            verify(albumService).updatePermissions(album.id, permissionsToAdd, permissionsToRemove, source)
        }

        @Test
        fun `should return 204`() = authenticated {
            whenever(userAlbumAuthzRepo.fetch(principal.user.id, album.id)).thenReturn(setOf(Permission.Update))
            whenever(userService.getAllByIds(usersIds)).thenReturn(users)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(dto),
                status = HttpStatus.NO_CONTENT
            )
            verify(userAlbumAuthzRepo).fetch(principal.user.id, album.id)
            verify(userService).getAllByIds(usersIds)
            verify(albumService).updatePermissions(album.id, permissionsToAdd, permissionsToRemove, source)
        }
    }
}
