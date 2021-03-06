package io.ivana.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.net.URI
import java.util.*

private const val DuplicateResourceCodeValue = "duplicate_resource"
private const val ForbiddenCodeValue = "forbidden"
private const val InternalErrorCodeValue = "internal_error"
private const val InvalidParameterCodeValue = "invalid_parameter"
private const val MalformedRequestCodeValue = "malformed_request"
private const val MethodNotAllowedCodeValue = "method_not_allowed"
private const val MissingParameterCodeValue = "missing_parameter"
private const val NotFoundCodeValue = "not_found"
private const val OwnerPermissionsUpdateCodeValue = "owner_permissions_update"
private const val PeopleAlreadyOnPhotoCodeValue = "people_already_on_photo"
private const val PhotoNotPresentInAlbumCodeValue = "photo_not_present_in_album"
private const val PhotosAlreadyInAlbumCodeValue = "album_already_contains_photos"
private const val ResourcesNotFoundCodeValue = "resources_not_found"
private const val UnauthorizedCodeValue = "unauthorized"
private const val UnsupportedMediaTypeCodeValue = "unsupported_media_type"
private const val ValidationErrorCodeValue = "validation_error"

@JsonTypeInfo(
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    use = JsonTypeInfo.Id.NAME,
    property = "code"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ErrorDto.DuplicateResource::class, name = DuplicateResourceCodeValue),
    JsonSubTypes.Type(value = ErrorDto.Forbidden::class, name = ForbiddenCodeValue),
    JsonSubTypes.Type(value = ErrorDto.InternalError::class, name = InternalErrorCodeValue),
    JsonSubTypes.Type(value = ErrorDto.InvalidParameter::class, name = InvalidParameterCodeValue),
    JsonSubTypes.Type(value = ErrorDto.MalformedRequest::class, name = MalformedRequestCodeValue),
    JsonSubTypes.Type(value = ErrorDto.MethodNotAllowed::class, name = MethodNotAllowedCodeValue),
    JsonSubTypes.Type(value = ErrorDto.MissingParameter::class, name = MissingParameterCodeValue),
    JsonSubTypes.Type(value = ErrorDto.NotFound::class, name = NotFoundCodeValue),
    JsonSubTypes.Type(value = ErrorDto.OwnerPermissionsUpdate::class, name = OwnerPermissionsUpdateCodeValue),
    JsonSubTypes.Type(value = ErrorDto.PeopleAlreadyOnPhotos::class, name = PeopleAlreadyOnPhotoCodeValue),
    JsonSubTypes.Type(value = ErrorDto.PhotoNotPresentInAlbum::class, name = PhotoNotPresentInAlbumCodeValue),
    JsonSubTypes.Type(value = ErrorDto.PhotosAlreadyInAlbum::class, name = PhotosAlreadyInAlbumCodeValue),
    JsonSubTypes.Type(value = ErrorDto.ResourcesNotFound::class, name = ResourcesNotFoundCodeValue),
    JsonSubTypes.Type(value = ErrorDto.Unauthorized::class, name = UnauthorizedCodeValue),
    JsonSubTypes.Type(value = ErrorDto.UnsupportedMediaType::class, name = UnsupportedMediaTypeCodeValue),
    JsonSubTypes.Type(value = ErrorDto.ValidationError::class, name = ValidationErrorCodeValue)
)
sealed class ErrorDto {
    enum class Code {
        @JsonProperty(DuplicateResourceCodeValue)
        DuplicateResource,

        @JsonProperty(ForbiddenCodeValue)
        Forbidden,

        @JsonProperty(InternalErrorCodeValue)
        InternalError,

        @JsonProperty(InvalidParameterCodeValue)
        InvalidParameter,

        @JsonProperty(MalformedRequestCodeValue)
        MalformedRequest,

        @JsonProperty(MethodNotAllowedCodeValue)
        MethodNotAllowed,

        @JsonProperty(MissingParameterCodeValue)
        MissingParameter,

        @JsonProperty(NotFoundCodeValue)
        NotFound,

        @JsonProperty(OwnerPermissionsUpdateCodeValue)
        OwnerPermissionsUpdate,

        @JsonProperty(PeopleAlreadyOnPhotoCodeValue)
        PeopleAlreadyOnPhoto,

        @JsonProperty(PhotoNotPresentInAlbumCodeValue)
        PhotoNotPresentInAlbum,

        @JsonProperty(PhotosAlreadyInAlbumCodeValue)
        PhotosAlreadyInAlbum,

        @JsonProperty(ResourcesNotFoundCodeValue)
        ResourcesNotFound,

        @JsonProperty(UnauthorizedCodeValue)
        Unauthorized,

        @JsonProperty(UnsupportedMediaTypeCodeValue)
        UnsupportedMediaType,

        @JsonProperty(ValidationErrorCodeValue)
        ValidationError
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

    data class InvalidParameter(
        val parameter: String,
        val reason: String
    ) : ErrorDto() {
        override val code = Code.InvalidParameter
    }

    object MalformedRequest : ErrorDto() {
        override val code = Code.MalformedRequest

        override fun equals(other: Any?) = other is MalformedRequest
    }

    object MethodNotAllowed : ErrorDto() {
        override val code = Code.MethodNotAllowed

        override fun equals(other: Any?) = other is MethodNotAllowed
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

    object OwnerPermissionsUpdate : ErrorDto() {
        override val code = Code.OwnerPermissionsUpdate

        override fun equals(other: Any?) = other is OwnerPermissionsUpdate
    }

    data class PeopleAlreadyOnPhotos(
        val peopleIds: Set<UUID>
    ) : ErrorDto() {
        override val code = Code.PeopleAlreadyOnPhoto
    }

    object PhotoNotPresentInAlbum : ErrorDto() {
        override val code = Code.PhotoNotPresentInAlbum

        override fun equals(other: Any?) = other is PhotoNotPresentInAlbum
    }

    data class PhotosAlreadyInAlbum(
        val photosIds: Set<UUID>
    ) : ErrorDto() {
        override val code = Code.PhotosAlreadyInAlbum
    }

    data class ResourcesNotFound(
        val type: Type,
        val ids: Set<UUID>
    ) : ErrorDto() {
        enum class Type {
            @JsonProperty("album")
            Album,

            @JsonProperty("person")
            Person,

            @JsonProperty("photo")
            Photo,

            @JsonProperty("user")
            User
        }

        override val code = Code.ResourcesNotFound
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

    data class ValidationError(
        val errors: List<Error>
    ) : ErrorDto() {
        data class Error(
            val parameter: String,
            val reason: String
        )

        override val code = Code.ValidationError
    }

    abstract val code: Code
}
