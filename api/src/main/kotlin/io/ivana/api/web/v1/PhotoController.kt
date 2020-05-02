package io.ivana.api.web.v1

import io.ivana.api.impl.PhotoAlreadyUploadedException
import io.ivana.api.security.CustomAuthentication
import io.ivana.api.security.PhotoTargetType
import io.ivana.api.security.UserPrincipal
import io.ivana.api.web.source
import io.ivana.core.*
import io.ivana.dto.*
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.net.URI
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Min

@RestController
@RequestMapping(PhotoApiEndpoint)
@Validated
class PhotoController(
    private val photoService: PhotoService,
    private val userService: UserService
) {
    internal companion object {
        val MediaTypeToPhotoType = mapOf(
            MediaType.IMAGE_JPEG_VALUE to Photo.Type.Jpg,
            MediaType.IMAGE_PNG_VALUE to Photo.Type.Png
        )

        private val Logger = LoggerFactory.getLogger(PhotoController::class.java)
    }

    @Transactional
    @DeleteMapping("/{id:$UuidRegex}")
    @PreAuthorize("hasPermission(#id, '$PhotoTargetType', 'delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun delete(@PathVariable id: UUID, auth: Authentication, req: HttpServletRequest) {
        val principal = auth.principal as UserPrincipal
        photoService.delete(id, req.source(principal))
    }

    @GetMapping("/{id:$UuidRegex}")
    @PreAuthorize("hasPermission(#id, '$PhotoTargetType', 'read')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun get(
        @PathVariable id: UUID,
        @RequestParam(name = NavigableParamName, required = false) navigable: Boolean = false
    ) = if (navigable) {
        photoService.getLinkedById(id).toNavigableDto()
    } else {
        photoService.getById(id).toSimpleDto()
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAll(
        @RequestParam(name = PageParamName, required = false, defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = SizeParamName, required = false, defaultValue = "10") @Min(1) size: Int,
        auth: Authentication
    ): PageDto<PhotoDto> {
        val principal = auth.principal as UserPrincipal
        return photoService.getAll(principal.user.id, page, size).toDto { it.toSimpleDto() }
    }

    @GetMapping("/{id:$UuidRegex}$CompressedPhotoEndpoint")
    @PreAuthorize("hasPermission(#id, '$PhotoTargetType', 'read')")
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun getCompressedFile(@PathVariable id: UUID) = photoService.getById(id).let { photo ->
        val file = photoService.getCompressedFile(photo)
        photoFileResponseEntity(file, photo.type)
    }

    @GetMapping("/{id:$UuidRegex}$PermissionsEndpoint")
    @PreAuthorize("hasPermission(#id, '$PhotoTargetType', 'update_permissions')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun getPermissions(
        @PathVariable id: UUID,
        @RequestParam(name = PageParamName, required = false, defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = SizeParamName, required = false, defaultValue = "10") @Min(1) size: Int
    ): PageDto<SubjectPermissionsDto> {
        val subjPerms = photoService.getPermissions(id, page, size)
        val usersIds = subjPerms.content.map { it.subjectId }.toSet()
        val users = userService.getAllByIds(usersIds).map { it.id to it }.toMap()
        return subjPerms.toDto { it.toDto(users.getValue(it.subjectId).name) }
    }

    @GetMapping("/{id:$UuidRegex}$RawPhotoEndpoint")
    @PreAuthorize("hasPermission(#id, '$PhotoTargetType', 'read')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun getRawFile(@PathVariable id: UUID) = photoService.getById(id).let { photo ->
        val file = photoService.getRawFile(photo)
        photoFileResponseEntity(file, photo.type)
    }

    @Transactional
    @PutMapping("/{id:$UuidRegex}$PermissionsEndpoint")
    @PreAuthorize("hasPermission(#id, '$PhotoTargetType', 'update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun updatePermissions(
        @PathVariable id: UUID,
        @RequestBody @Valid dto: PhotoUpdatePermissionsDto,
        auth: Authentication,
        req: HttpServletRequest
    ) {
        val principal = auth.principal as UserPrincipal
        val usersIds = (dto.permissionsToAdd + dto.permissionsToRemove).map { it.subjectId }.toSet()
        val users = userService.getAllByIds(usersIds).map { it.id to it }.toMap()
        photoService.updatePermissions(
            id = id,
            permissionsToAdd = dto.permissionsToAdd.toUserPermissionsSet(users),
            permissionsToRemove = dto.permissionsToRemove.toUserPermissionsSet(users),
            source = req.source(principal)
        )
    }

    @Transactional
    @PutMapping("/{id:$UuidRegex}$TransformPhotoEndpoint")
    @PreAuthorize("hasPermission(#id, '$PhotoTargetType', 'update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun transform(
        @PathVariable id: UUID,
        @RequestBody @Valid transformDto: TransformDto,
        auth: Authentication,
        req: HttpServletRequest
    ) {
        photoService.transform(id, transformDto.toTransform(), req.source(auth.principal as UserPrincipal))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun upload(
        @RequestParam(FilesParamName) files: List<MultipartFile>, auth: Authentication, req: HttpServletRequest
    ): PhotoUploadResultsDto {
        val principal = (auth as CustomAuthentication).principal
        val source = req.source(principal)
        return PhotoUploadResultsDto(files.map { uploadPhoto(it, source) })
    }

    private fun photoFileResponseEntity(file: File, type: Photo.Type): ResponseEntity<FileSystemResource> {
        val contentType = MediaTypeToPhotoType.entries
            .find { it.value == type }!!
            .key
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.parseMediaType(contentType))
            .body(FileSystemResource(file))
    }

    private fun uploadPhoto(file: MultipartFile, source: EventSource.User): PhotoUploadResultsDto.Result {
        val contentType = file.contentType
        return if (contentType == null || !MediaTypeToPhotoType.containsKey(contentType)) {
            PhotoUploadResultsDto.Result.Failure(
                ErrorDto.UnsupportedMediaType(MediaTypeToPhotoType.keys)
            )
        } else {
            try {
                val photo = photoService.upload(file.inputStream, MediaTypeToPhotoType.getValue(contentType), source)
                PhotoUploadResultsDto.Result.Success(photo.toSimpleDto())
            } catch (exception: PhotoAlreadyUploadedException) {
                PhotoUploadResultsDto.Result.Failure(
                    ErrorDto.DuplicateResource(URI("$PhotoApiEndpoint/${exception.photo.id}"))
                )
            } catch (exception: Exception) {
                Logger.error(exception.message, exception)
                PhotoUploadResultsDto.Result.Failure(ErrorDto.InternalError)
            }
        }
    }

    private fun PermissionDto.toPermission() = when (this) {
        PermissionDto.Read -> Permission.Read
        PermissionDto.Update -> Permission.Update
        PermissionDto.Delete -> Permission.Delete
        PermissionDto.UpdatePermissions -> Permission.UpdatePermissions
    }

    private fun Set<SubjectPermissionsUpdateDto>.toUserPermissionsSet(users: Map<UUID, User>) =
        map { dto ->
            UserPermissions(
                user = users.getValue(dto.subjectId),
                permissions = dto.permissions.map { it.toPermission() }.toSet()
            )
        }.toSet()

    private fun TransformDto.toTransform() = when (this) {
        is TransformDto.Rotation -> Transform.Rotation(degrees)
    }
}
