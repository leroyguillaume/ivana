package io.ivana.api.web

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.ivana.api.impl.*
import io.ivana.api.security.BadCredentialsException
import io.ivana.api.security.BadJwtException
import io.ivana.api.security.CustomAccessDeniedHandler
import io.ivana.api.web.v1.PermParamName
import io.ivana.api.web.v1.PersonApiEndpoint
import io.ivana.api.web.v1.UserApiEndpoint
import io.ivana.core.Permission
import io.ivana.dto.ErrorDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MultipartException
import java.net.URI
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

@RestControllerAdvice
class ErrorController(
    private val mapper: ObjectMapper
) {
    private companion object {
        val Logger = LoggerFactory.getLogger(ErrorController::class.java)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(exception: AccessDeniedException, req: HttpServletRequest, resp: HttpServletResponse) =
        CustomAccessDeniedHandler(mapper).handle(req, resp, exception)

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolation(exception: ConstraintViolationException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.ValidationError(
            errors = exception.constraintViolations.map { it.toDto() }
        )
    }

    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleEntityNotFound(exception: EntityNotFoundException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.NotFound
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(exception: Exception): ErrorDto {
        Logger.error(exception.message, exception)
        return ErrorDto.InternalError
    }

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbidden(exception: Exception): ErrorDto {
        Logger.warn(exception.message, exception)
        return ErrorDto.Forbidden
    }

    @ExceptionHandler(value = [HttpMessageNotReadableException::class, MultipartException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMalformedRequest(exception: Exception): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.MalformedRequest
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValid(exception: MethodArgumentNotValidException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.ValidationError(
            errors = exception.bindingResult.fieldErrors
                .map { ErrorDto.ValidationError.Error(it.field, it.defaultMessage!!) }
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentTypeMismatch(exception: MethodArgumentTypeMismatchException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.InvalidParameter(
            parameter = exception.name,
            reason = "must be ${exception.requiredType!!.simpleName.toLowerCase()}"
        )
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    fun handleMethodNotSupported(exception: HttpRequestMethodNotSupportedException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.MethodNotAllowed
    }

    @ExceptionHandler(HttpMessageConversionException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingParameter(exception: HttpMessageConversionException): ErrorDto {
        Logger.debug(exception.message, exception)
        return when (val cause = exception.cause) {
            is MissingKotlinParameterException -> ErrorDto.MissingParameter(cause.path.toHumanReadablePath())
            else -> handleException(exception)
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingServletRequestParameter(exception: MissingServletRequestParameterException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.MissingParameter(exception.parameterName)
    }

    @ExceptionHandler(OwnerPermissionsUpdateException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleOwnerPermissionsUpdate(exception: OwnerPermissionsUpdateException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.OwnerPermissionsUpdate
    }

    @ExceptionHandler(PeopleAlreadyOnPhotoException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handlePeopleAlreadyOnPhoto(exception: PeopleAlreadyOnPhotoException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.PeopleAlreadyOnPhotos(exception.peopleIds)
    }

    @ExceptionHandler(PhotosAlreadyInAlbumException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handlePhotosAlreadyInAlbum(exception: PhotosAlreadyInAlbumException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.PhotosAlreadyInAlbum(exception.photosIds)
    }

    @ExceptionHandler(ResourcesNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handlePhotosNotFound(exception: ResourcesNotFoundException): ErrorDto {
        Logger.debug(exception.message, exception)
        val type = when (exception) {
            is ResourcesNotFoundException.Album -> ErrorDto.ResourcesNotFound.Type.Album
            is ResourcesNotFoundException.Person -> ErrorDto.ResourcesNotFound.Type.Person
            is ResourcesNotFoundException.Photo -> ErrorDto.ResourcesNotFound.Type.Photo
            is ResourcesNotFoundException.User -> ErrorDto.ResourcesNotFound.Type.User
        }
        return ErrorDto.ResourcesNotFound(type, exception.ids)
    }

    @ExceptionHandler(PhotoNotPresentInAlbumException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handlePhotoNotPresentInAlbum(exception: PhotoNotPresentInAlbumException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.PhotoNotPresentInAlbum
    }

    @ExceptionHandler(value = [BadCredentialsException::class, BadJwtException::class])
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorized(exception: Exception): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.Unauthorized
    }

    @ExceptionHandler(UnknownPermissionException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUnknownPermission(exception: UnknownPermissionException): ErrorDto {
        Logger.debug(exception.message, exception)
        val permLabels = Permission.values().map { it.label }.reduce { acc, label -> "$acc, $label" }
        return ErrorDto.InvalidParameter(
            parameter = PermParamName,
            reason = "must be one of [$permLabels]"
        )
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    fun handleUnsupportedMediaType(exception: HttpMediaTypeNotSupportedException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.UnsupportedMediaType(setOf(MediaType.APPLICATION_JSON_VALUE))
    }

    @ExceptionHandler(PersonAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handlePersonAlreadyExists(exception: PersonAlreadyExistsException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.DuplicateResource(URI("$PersonApiEndpoint/${exception.person.id}"))
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleUserAlreadyExists(exception: UserAlreadyExistsException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.DuplicateResource(URI("$UserApiEndpoint/${exception.user.id}"))
    }

    private fun ConstraintViolation<*>.toDto() = ErrorDto.ValidationError.Error(
        parameter = propertyPath
            .drop(1)
            .map { it.name }
            .reduce { acc, property -> "$acc.$property" },
        reason = message
    )

    private fun List<JsonMappingException.Reference>.toHumanReadablePath() = map { it.fieldName }
        .reduce { field1, field2 -> "$field1.$field2" }
}
