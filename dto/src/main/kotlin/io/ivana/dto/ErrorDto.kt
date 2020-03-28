package io.ivana.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

private const val ForbiddenCodeValue = "forbidden"
private const val InternalErrorCodeValue = "internal_error"
private const val InvalidContentTypeCodeValue = "invalid_content_type"
private const val MalformedRequestCodeValue = "malformed_request"
private const val MissingParameterCodeValue = "missing_parameter"
private const val NotFoundCodeValue = "not_found"
private const val UnauthorizedCodeValue = "unauthorized"

@JsonTypeInfo(
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    use = JsonTypeInfo.Id.NAME,
    property = "code"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ErrorDto.Forbidden::class, name = ForbiddenCodeValue),
    JsonSubTypes.Type(value = ErrorDto.InternalError::class, name = InternalErrorCodeValue),
    JsonSubTypes.Type(value = ErrorDto.InvalidContentType::class, name = InvalidContentTypeCodeValue),
    JsonSubTypes.Type(value = ErrorDto.MalformedRequest::class, name = MalformedRequestCodeValue),
    JsonSubTypes.Type(value = ErrorDto.MissingParameter::class, name = MissingParameterCodeValue),
    JsonSubTypes.Type(value = ErrorDto.NotFound::class, name = NotFoundCodeValue),
    JsonSubTypes.Type(value = ErrorDto.Unauthorized::class, name = UnauthorizedCodeValue)
)
sealed class ErrorDto {
    enum class Code {
        @JsonProperty(ForbiddenCodeValue)
        Forbidden,

        @JsonProperty(InternalErrorCodeValue)
        InternalError,

        @JsonProperty(InvalidContentTypeCodeValue)
        InvalidContentType,

        @JsonProperty(MalformedRequestCodeValue)
        MalformedRequest,

        @JsonProperty(MissingParameterCodeValue)
        MissingParameter,

        @JsonProperty(NotFoundCodeValue)
        NotFound,

        @JsonProperty(UnauthorizedCodeValue)
        Unauthorized
    }

    object Forbidden : ErrorDto() {
        override val code = Code.Forbidden

        override fun equals(other: Any?) = other is Forbidden
    }

    data class InvalidContentType(
        val supportedContentTypes: Set<String>
    ) : ErrorDto() {
        override val code = Code.InvalidContentType
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

    abstract val code: Code
}
