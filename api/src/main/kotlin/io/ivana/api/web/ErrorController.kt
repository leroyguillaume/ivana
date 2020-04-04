package io.ivana.api.web

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.ivana.api.impl.EntityNotFoundException
import io.ivana.api.security.BadCredentialsException
import io.ivana.api.security.BadJwtException
import io.ivana.api.security.CustomAccessDeniedHandler
import io.ivana.dto.ErrorDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MultipartException
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

    @ExceptionHandler(value = [HttpMessageNotReadableException::class, MultipartException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMalformedRequest(exception: Exception): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.MalformedRequest
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

    @ExceptionHandler(HttpMessageConversionException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingParameter(exception: HttpMessageConversionException): ErrorDto {
        Logger.debug(exception.message, exception)
        return when (val cause = exception.cause) {
            is MissingKotlinParameterException -> ErrorDto.MissingParameter(cause.path.toHumanReadablePath())
            else -> handleException(exception)
        }
    }

    @ExceptionHandler(value = [BadCredentialsException::class, BadJwtException::class])
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorized(exception: Exception): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.Unauthorized
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    fun handleUnsupportedMediaType(exception: HttpMediaTypeNotSupportedException): ErrorDto {
        Logger.debug(exception.message, exception)
        return ErrorDto.UnsupportedMediaType(setOf(MediaType.APPLICATION_JSON_VALUE))
    }

    private fun ConstraintViolation<*>.toDto() = ErrorDto.InvalidParameter(
        parameter = propertyPath
            .drop(1)
            .map { it.name }
            .reduce { acc, property -> "$acc.$property" },
        reason = message
    )

    private fun List<JsonMappingException.Reference>.toHumanReadablePath() = map { it.fieldName }
        .reduce { field1, field2 -> "$field1.$field2" }
}
