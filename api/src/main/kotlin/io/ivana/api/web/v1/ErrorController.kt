package io.ivana.api.web.v1

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.ivana.api.security.BadCredentialsException
import io.ivana.dto.ErrorDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
class ErrorController {
    private companion object {
        val Logger = LoggerFactory.getLogger(ErrorController::class.java)
    }

    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleBadCredentialsException(exception: BadCredentialsException) = ErrorDto.Unauthorized

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(exception: Exception): ErrorDto {
        Logger.error(exception.message, exception)
        return ErrorDto.InternalError
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    fun handleInvalidContentType(exception: HttpMediaTypeNotSupportedException) = ErrorDto.InvalidContentType(
        setOf(MediaType.APPLICATION_JSON_VALUE)
    )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMalformedRequest(exception: HttpMessageNotReadableException) = ErrorDto.MalformedRequest

    @ExceptionHandler(HttpMessageConversionException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingParameter(exception: HttpMessageConversionException) = when (val cause = exception.cause) {
        is MissingKotlinParameterException -> ErrorDto.MissingParameter(cause.path.toHumanReadablePath())
        else -> handleException(exception)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(exception: NoHandlerFoundException) = ErrorDto.NotFound

    private fun List<JsonMappingException.Reference>.toHumanReadablePath() = map { it.fieldName }
        .reduce { field1, field2 -> "$field1.$field2" }
}
