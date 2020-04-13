@file:Suppress("ClassName")

package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.verify
import io.ivana.api.web.AbstractControllerTest
import io.ivana.dto.ErrorDto
import io.ivana.dto.PasswordUpdateDto
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest
@AutoConfigureMockMvc
internal class UserControllerTest : AbstractControllerTest() {
    @Nested
    inner class updatePassword {
        private val method = HttpMethod.PUT
        private val uri = "$UserApiEndpoint$PasswordUpdateEndpoint"
        private val dto = PasswordUpdateDto("changeit")

        @Test
        fun `should return 401 if user is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 204`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(dto),
                status = HttpStatus.NO_CONTENT
            )
            verify(userService).updatePassword(principal.user.id, dto.newPwd, source)
        }
    }
}
