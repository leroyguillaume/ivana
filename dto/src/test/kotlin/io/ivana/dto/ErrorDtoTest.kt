package io.ivana.dto

import org.junit.jupiter.api.Nested
import java.net.URI
import java.util.*

internal class ErrorDtoTest {
    @Nested
    inner class DuplicateResource : JsonTest(
        filename = "error/duplicate-resource.json",
        expectedValue = ErrorDto.DuplicateResource(URI("/v1/photo/61f11547-a340-441c-bce7-551234d5d361")),
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class Forbidden : JsonTest(
        filename = "error/forbidden.json",
        expectedValue = ErrorDto.Forbidden,
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class InternalError : JsonTest(
        filename = "error/internal-error.json",
        expectedValue = ErrorDto.InternalError,
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class InvalidParameter : JsonTest(
        filename = "error/invalid-parameter.json",
        expectedValue = ErrorDto.InvalidParameter(
            parameter = "foo",
            reason = "must be boolean"
        ),
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class MalformedRequest : JsonTest(
        filename = "error/malformed-request.json",
        expectedValue = ErrorDto.MalformedRequest,
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class MethodNotAllowed : JsonTest(
        filename = "error/method-not-allowed.json",
        expectedValue = ErrorDto.MethodNotAllowed,
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class MissingParameter : JsonTest(
        filename = "error/missing-parameter.json",
        expectedValue = ErrorDto.MissingParameter("foo"),
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class NotFound : JsonTest(
        filename = "error/not-found.json",
        expectedValue = ErrorDto.NotFound,
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class OwnerPermissionsUpdate : JsonTest(
        filename = "error/owner-permissions-update.json",
        expectedValue = ErrorDto.OwnerPermissionsUpdate,
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class PeopleAlreadyOnPhoto : JsonTest(
        filename = "error/people-already-on-photo.json",
        expectedValue = ErrorDto.PeopleAlreadyOnPhotos(
            peopleIds = setOf(UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"))
        ),
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class PhotoNotPresentInAlbum : JsonTest(
        filename = "error/photo-not-present-in-album.json",
        expectedValue = ErrorDto.PhotoNotPresentInAlbum,
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class PhotosAlreadyInAlbum : JsonTest(
        filename = "error/photos-already-in-album.json",
        expectedValue = ErrorDto.PhotosAlreadyInAlbum(
            photosIds = setOf(UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"))
        ),
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class ResourcesNotFound {
        @Nested
        inner class Album : JsonTest(
            filename = "error/resources-not-found/album.json",
            expectedValue = ErrorDto.ResourcesNotFound(
                type = ErrorDto.ResourcesNotFound.Type.Album,
                ids = setOf(UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"))
            ),
            deserializeAs = typeOf<ErrorDto>()
        )

        @Nested
        inner class Person : JsonTest(
            filename = "error/resources-not-found/person.json",
            expectedValue = ErrorDto.ResourcesNotFound(
                type = ErrorDto.ResourcesNotFound.Type.Person,
                ids = setOf(UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"))
            ),
            deserializeAs = typeOf<ErrorDto>()
        )

        @Nested
        inner class Photo : JsonTest(
            filename = "error/resources-not-found/photo.json",
            expectedValue = ErrorDto.ResourcesNotFound(
                type = ErrorDto.ResourcesNotFound.Type.Photo,
                ids = setOf(UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"))
            ),
            deserializeAs = typeOf<ErrorDto>()
        )

        @Nested
        inner class User : JsonTest(
            filename = "error/resources-not-found/user.json",
            expectedValue = ErrorDto.ResourcesNotFound(
                type = ErrorDto.ResourcesNotFound.Type.User,
                ids = setOf(UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"))
            ),
            deserializeAs = typeOf<ErrorDto>()
        )
    }

    @Nested
    inner class Unauthorized : JsonTest(
        filename = "error/unauthorized.json",
        expectedValue = ErrorDto.Unauthorized,
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class UnsupportedMediaType : JsonTest(
        filename = "error/unsupported-media-type.json",
        expectedValue = ErrorDto.UnsupportedMediaType(setOf("application/json")),
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class ValidationError : JsonTest(
        filename = "error/validation-error.json",
        expectedValue = ErrorDto.ValidationError(
            listOf(
                ErrorDto.ValidationError.Error("foo", "must not be null"),
                ErrorDto.ValidationError.Error("bar", "must be greater than 1")
            )
        ),
        deserializeAs = typeOf<ErrorDto>()
    )
}
