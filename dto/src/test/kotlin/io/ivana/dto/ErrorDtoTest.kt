package io.ivana.dto

import org.junit.jupiter.api.Nested

internal class ErrorDtoTest {
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
    inner class InvalidContentType : JsonTest(
        filename = "error/invalid-content-type.json",
        expectedValue = ErrorDto.InvalidContentType(setOf("application/json")),
        deserializeAs = typeOf<ErrorDto>()
    )

    @Nested
    inner class MalformedRequest : JsonTest(
        filename = "error/malformed-request.json",
        expectedValue = ErrorDto.MalformedRequest,
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
    inner class Unauthorized : JsonTest(
        filename = "error/unauthorized.json",
        expectedValue = ErrorDto.Unauthorized,
        deserializeAs = typeOf<ErrorDto>()
    )
}
