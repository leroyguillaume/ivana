package io.ivana.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.net.URI

private const val DuplicateResourceCodeValue = "duplicate_resource"
private const val ForbiddenCodeValue = "forbidden"
private const val InternalErrorCodeValue = "internal_error"
private const val MalformedRequestCodeValue = "malformed_request"
private const val MissingParameterCodeValue = "missing_parameter"
private const val NotFoundCodeValue = "not_found"
private const val UnauthorizedCodeValue = "unauthorized"
private const val UnsupportedMediaTypeCodeValue = "unsupported_media_type"

@JsonTypeInfo(
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    use = JsonTypeInfo.Id.NAME,
    property = "code"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ErrorDto.DuplicateResource::class, name = DuplicateResourceCodeValue),
    JsonSubTypes.Type(value = ErrorDto.Forbidden::class, name = ForbiddenCodeValue),
    JsonSubTypes.Type(value = ErrorDto.InternalError::class, name = InternalErrorCodeValue),
    JsonSubTypes.Type(value = ErrorDto.MalformedRequest::class, name = MalformedRequestCodeValue),
    JsonSubTypes.Type(value = ErrorDto.MissingParameter::class, name = MissingParameterCodeValue),
    JsonSubTypes.Type(value = ErrorDto.NotFound::class, name = NotFoundCodeValue),
    JsonSubTypes.Type(value = ErrorDto.Unauthorized::class, name = UnauthorizedCodeValue),
    JsonSubTypes.Type(value = ErrorDto.UnsupportedMediaType::class, name = UnsupportedMediaTypeCodeValue)
)
sealed class ErrorDto {
    enum class Code {
        @JsonProperty(DuplicateResourceCodeValue)
        DuplicateResource,

        @JsonProperty(ForbiddenCodeValue)
        Forbidden,

        @JsonProperty(InternalErrorCodeValue)
        InternalError,

        @JsonProperty(MalformedRequestCodeValue)
        MalformedRequest,

        @JsonProperty(MissingParameterCodeValue)
        MissingParameter,

        @JsonProperty(NotFoundCodeValue)
        NotFound,

        @JsonProperty(UnauthorizedCodeValue)
        Unauthorized,

        @JsonProperty(UnsupportedMediaTypeCodeValue)
        UnsupportedMediaType
    }

    data class DuplicateResource(
        val resourceUri: URI
    ) : ErrorDto() {
        override val code = Code.DuplicateResource
    }

    object Forbidden : ErrorDto() {
        override val code = Code.Forbidden

        override fun equals(other: Any?) = other is Forbidden
    }

    object InternalError : ErrorDto() {
        override val code = Code.InternalError

        override fun equals(other: Any?) = other is InternalError
    }

    object MalformedRequest : ErrorDto() {
        override val code = Code.MalformedRequest

        override fun equals(other: Any?) = other is MalformedRequest
    }

    data class MissingParameter(
        val parameter: String
    ) : ErrorDto() {
        override val code = Code.MissingParameter
    }

    object NotFound : ErrorDto() {
        override val code = Code.NotFound

        override fun equals(other: Any?) = other is NotFound
    }

    object Unauthorized : ErrorDto() {
        override val code = Code.Unauthorized

        override fun equals(other: Any?) = other is Unauthorized
    }

    data class UnsupportedMediaType(
        val supportedMediaTypes: Set<String>
    ) : ErrorDto() {
        override val code = Code.UnsupportedMediaType
    }

    abstract val code: Code
}
