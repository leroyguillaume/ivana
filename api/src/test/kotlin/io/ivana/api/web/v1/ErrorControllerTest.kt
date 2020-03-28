package io.ivana.api.web.v1

import io.ivana.dto.ErrorDto
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest
@AutoConfigureMockMvc
internal class ErrorControllerTest : AbstractControllerTest() {
    @Test
    fun `should return 404 if endpoint does not exist`() {
        callAndExpect(
            method = HttpMethod.GET,
            uri = "/",
            status = HttpStatus.NOT_FOUND,
            respDto = ErrorDto.NotFound
        )
    }

    @Test
    fun `should return 415 if content type is invalid`() {
        callAndExpect(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            contentType = MediaType.APPLICATION_PDF,
            reqContent = "{}",
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            respDto = ErrorDto.InvalidContentType(setOf(MediaType.APPLICATION_JSON_VALUE))
        )
    }

    @Test
    fun `should return 400 if content is empty`() {
        callAndExpect(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            reqContent = "",
            status = HttpStatus.BAD_REQUEST,
            respDto = ErrorDto.MalformedRequest
        )
    }

    @Test
    fun `should return 400 if content is malformed`() {
        callAndExpect(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            reqContent = "{",
            status = HttpStatus.BAD_REQUEST,
            respDto = ErrorDto.MalformedRequest
        )
    }

    @Test
    fun `should return 400 if missing parameter`() {
        callAndExpect(
            method = HttpMethod.POST,
            uri = LoginEndpoint,
            reqContent = "{}",
            status = HttpStatus.BAD_REQUEST,
            respDto = ErrorDto.MissingParameter("username")
        )
    }
}
