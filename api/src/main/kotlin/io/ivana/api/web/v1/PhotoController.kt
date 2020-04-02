package io.ivana.api.web.v1

import io.ivana.api.impl.PhotoAlreadyUploadedException
import io.ivana.api.security.CustomAuthentication
import io.ivana.api.security.UserPhotoTargetType
import io.ivana.core.EventSource
import io.ivana.core.Photo
import io.ivana.core.PhotoService
import io.ivana.dto.ErrorDto
import io.ivana.dto.PhotoUploadResultsDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.InetAddress
import java.net.URI
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(PhotoApiEndpoint)
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
    fun get(@PathVariable id: UUID) = photoService.getById(id).toDto()

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
                PhotoUploadResultsDto.Result.Success(photo.toDto())
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
