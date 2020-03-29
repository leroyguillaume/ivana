package io.ivana.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

private const val SuccessResultTypeValue = "success"
private const val FailureResultTypeValue = "failure"

data class PhotoUploadResultsDto(
    val results: List<Result> = emptyList()
) {
    @JsonTypeInfo(
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        use = JsonTypeInfo.Id.NAME,
        property = "type"
    )
    @JsonSubTypes(
        JsonSubTypes.Type(value = Result.Success::class, name = SuccessResultTypeValue),
        JsonSubTypes.Type(value = Result.Failure::class, name = FailureResultTypeValue)
    )
    sealed class Result {
        enum class Type {
            @JsonProperty(SuccessResultTypeValue)
            Success,

            @JsonProperty(FailureResultTypeValue)
            Failure
        }

        data class Success(
            val photo: PhotoDto
        ) : Result() {
            override val type = Type.Success
        }

        data class Failure(
            val error: ErrorDto
        ) : Result() {
            override val type = Type.Failure
        }

        abstract val type: Type
    }
}
