package io.ivana.api.web.v1

import io.ivana.api.impl.PhotoAlreadyUploadedException
import io.ivana.api.security.CustomAuthentication
import io.ivana.api.security.UserPhotoTargetType
import io.ivana.api.security.UserPrincipal
import io.ivana.core.EventSource
import io.ivana.core.Photo
import io.ivana.core.PhotoService
import io.ivana.dto.ErrorDto
import io.ivana.dto.PageDto
import io.ivana.dto.PhotoDto
import io.ivana.dto.PhotoUploadResultsDto
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
import java.net.InetAddress
import java.net.URI
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.constraints.Min

@RestController
@RequestMapping(PhotoApiEndpoint)
@Validated
class PhotoController(
    private val photoService: PhotoService
) {
    internal companion object {
        val MediaTypeToPhotoType = mapOf(
            MediaType.IMAGE_JPEG_VALUE to Photo.Type.Jpg,
            MediaType.IMAGE_PNG_VALUE to Photo.Type.Png
        )

        private val Logger = LoggerFactory.getLogger(PhotoController::class.java)
    }

    @GetMapping("/{id:$UuidRegex}")
    @PreAuthorize("hasPermission(#id, '$UserPhotoTargetType', 'read')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun get(
        @PathVariable id: UUID,
        @RequestParam(name = NavigableParamName, required = false) navigable: Boolean = false
    ) = if (navigable) {
        photoService.getTimeWindowById(id).toNavigableDto()
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
    @PreAuthorize("hasPermission(#id, '$UserPhotoTargetType', 'read')")
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun getCompressedFile(@PathVariable id: UUID) = photoService.getById(id).let { photo ->
        val file = photoService.getCompressedFile(photo)
        photoFileResponseEntity(file, photo.type)
    }

    @GetMapping("/{id:$UuidRegex}$RawPhotoEndpoint")
    @PreAuthorize("hasPermission(#id, '$UserPhotoTargetType', 'read')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun getRawFile(@PathVariable id: UUID) = photoService.getById(id).let { photo ->
        val file = photoService.getRawFile(photo)
        photoFileResponseEntity(file, photo.type)
    }

    @Transactional
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun upload(
        @RequestParam(FilesParamName) files: List<MultipartFile>, auth: Authentication, req: HttpServletRequest
    ): PhotoUploadResultsDto {
        val principal = (auth as CustomAuthentication).principal
        val source = EventSource.User(principal.user.id, InetAddress.getByName(req.remoteAddr))
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
                val photo = photoService.uploadPhoto(
                    file.inputStream, MediaTypeToPhotoType.getValue(contentType), source
                )
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
}
