@file:Suppress("ClassName")

package io.ivana.api.web.v1

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ivana.api.impl.PersonAlreadyExistsException
import io.ivana.api.web.AbstractControllerTest
import io.ivana.core.Page
import io.ivana.core.Person
import io.ivana.core.PersonEvent
import io.ivana.dto.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.net.URI
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
internal class PersonControllerTest : AbstractControllerTest() {
    @Nested
    inner class create {
        private val method = HttpMethod.POST
        private val uri = PersonApiEndpoint
        private val creationDto = PersonCreationDto(
            lastName = "Leroy",
            firstName = "Guillaume"
        )
        private val eventContent = PersonEvent.Creation.Content(
            lastName = creationDto.lastName,
            firstName = creationDto.firstName
        )
        private val person = Person(
            id = UUID.randomUUID(),
            lastName = creationDto.lastName,
            firstName = creationDto.firstName
        )
        private val personDto = person.toDto()

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
        fun `should return 400 if params are too short`() = authenticated(adminPrincipal) {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(
                    creationDto.copy(
                        lastName = "",
                        firstName = ""
                    )
                ),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(
                        sizeErrorDto("lastName", PersonLastNameMinSize, PersonLastNameMaxSize),
                        sizeErrorDto("firstName", PersonFirstNameMinSize, PersonFirstNameMaxSize)
                    )
                )
            )
        }

        @Test
        fun `should return 400 if name is too long`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(
                    creationDto.copy(
                        lastName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        firstName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                    )
                ),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(
                        sizeErrorDto("lastName", PersonLastNameMinSize, PersonLastNameMaxSize),
                        sizeErrorDto("firstName", PersonFirstNameMinSize, PersonFirstNameMaxSize)
                    )
                )
            )
        }

        @Test
        fun `should return 409 if person already exists`() = authenticated(adminPrincipal) {
            whenever(personService.create(eventContent, source)).thenAnswer {
                throw PersonAlreadyExistsException(person)
            }
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(creationDto),
                status = HttpStatus.CONFLICT,
                respDto = ErrorDto.DuplicateResource(URI("$PersonApiEndpoint/${person.id}"))
            )
            verify(personService).create(eventContent, source)
        }

        @Test
        fun `should return 403 if person is not admin or super admin`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(creationDto),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
        }

        @Test
        fun `should return 201 (admin)`() = authenticated(adminPrincipal) {
            whenever(personService.create(eventContent, source)).thenReturn(person)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(creationDto),
                status = HttpStatus.CREATED,
                respDto = personDto
            )
            verify(personService).create(eventContent, source)
        }

        @Test
        fun `should return 201 (super_admin)`() = authenticated(superAdminPrincipal) {
            whenever(personService.create(eventContent, source)).thenReturn(person)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(creationDto),
                status = HttpStatus.CREATED,
                respDto = personDto
            )
            verify(personService).create(eventContent, source)
        }
    }

    @Nested
    inner class delete {
        private val personId = UUID.randomUUID()
        private val method = HttpMethod.DELETE
        private val uri = "$PersonApiEndpoint/$personId"

        @Test
        fun `should return 401 if person is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 403 if person does not exist`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
        }

        @Test
        fun `should return 403 if user tries to delete person`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
        }

        @Test
        fun `should return 204 if admin deleted person`() = authenticated(adminPrincipal) {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.NO_CONTENT
            )
            verify(personService).delete(personId, source)
        }

        @Test
        fun `should return 204 if super admin deleted person`() = authenticated(superAdminPrincipal) {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.NO_CONTENT
            )
            verify(personService).delete(personId, source)
        }
    }

    @Nested
    inner class getById {
        private val person = Person(
            id = UUID.randomUUID(),
            lastName = "Leroy",
            firstName = "Guillaume"
        )
        private val dto = person.toDto()
        private val method = HttpMethod.GET
        private val uri = "$PersonApiEndpoint/${person.id}"

        @Test
        fun `should return 401 if person is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(personService.getById(person.id)).thenReturn(person)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = dto
            )
            verify(personService).getById(person.id)
        }
    }

    @Nested
    inner class getAll {
        private val pageNo = 2
        private val pageSize = 3
        private val page = Page(
            content = listOf(
                Person(
                    id = UUID.randomUUID(),
                    lastName = "Leroy",
                    firstName = "Guillaume"
                ),
                Person(
                    id = UUID.randomUUID(),
                    lastName = "Leroy",
                    firstName = "Annie"
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )
        private val pageDto = page.toDto { it.toDto() }
        private val method = HttpMethod.GET
        private val uri = PersonApiEndpoint

        @Test
        fun `should return 401 if person is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 400 if parameters are lower than 1`() = authenticated(adminPrincipal) {
            callAndExpectDto(
                method = method,
                params = mapOf(
                    PageParamName to listOf("-1"),
                    SizeParamName to listOf("-1")
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(minErrorDto(PageParamName, 1), minErrorDto(SizeParamName, 1))
                )
            )
        }

        @Test
        fun `should return 403 if person is not admin or super admin`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
        }

        @Test
        fun `should return 200 (admin)`() = authenticated(adminPrincipal) {
            whenever(personService.getAll(pageNo, pageSize)).thenReturn(page)
            callAndExpectDto(
                method = method,
                params = mapOf(
                    PageParamName to listOf(pageNo.toString()),
                    SizeParamName to listOf(pageSize.toString())
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = pageDto
            )
            verify(personService).getAll(pageNo, pageSize)
        }

        @Test
        fun `should return 200 (super admin)`() = authenticated(superAdminPrincipal) {
            whenever(personService.getAll(pageNo, pageSize)).thenReturn(page)
            callAndExpectDto(
                method = method,
                params = mapOf(
                    PageParamName to listOf(pageNo.toString()),
                    SizeParamName to listOf(pageSize.toString())
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = pageDto
            )
            verify(personService).getAll(pageNo, pageSize)
        }
    }

    @Nested
    inner class suggest {
        private val people = listOf(
            Person(
                id = UUID.randomUUID(),
                lastName = "Leroy",
                firstName = "Guillaume"
            ),
            Person(
                id = UUID.randomUUID(),
                lastName = "Leroy",
                firstName = "Annie"
            )
        )
        private val q = " ero "
        private val count = 10
        private val dto = people.map { it.toDto() }
        private val method = HttpMethod.GET
        private val uri = "$PersonApiEndpoint$SuggestEndpoint"

        @Test
        fun `should return 401 if person is anonymous`() {
            callAndExpectDto(
                method = method,
                uri = uri,
                status = HttpStatus.UNAUTHORIZED,
                respDto = ErrorDto.Unauthorized
            )
        }

        @Test
        fun `should return 400 if parameters are blank`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                params = mapOf(
                    QParamName to listOf(" ")
                ),
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(blankErrorDto(QParamName))
                )
            )
        }

        @Test
        fun `should return 400 if parameters are lower than 1`() = authenticated {
            callAndExpectDto(
                method = method,
                params = mapOf(
                    QParamName to listOf(q),
                    CountParamName to listOf("0")
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(minErrorDto(CountParamName, 1))
                )
            )
        }

        @Test
        fun `should return 200`() = authenticated {
            whenever(personService.suggest(q.trim(), count)).thenReturn(people)
            callAndExpectDto(
                method = method,
                params = mapOf(
                    QParamName to listOf(q),
                    CountParamName to listOf(count.toString())
                ),
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                status = HttpStatus.OK,
                respDto = dto
            )
            verify(personService).suggest(q.trim(), count)
        }
    }

    @Nested
    inner class update {
        private val updateDto = PersonUpdateDto(
            lastName = "Leroy",
            firstName = "Guillaume"
        )
        private val eventContent = PersonEvent.Update.Content(
            lastName = updateDto.lastName,
            firstName = updateDto.firstName
        )
        private val person = Person(
            id = UUID.randomUUID(),
            lastName = updateDto.lastName,
            firstName = updateDto.firstName
        )
        private val personDto = person.toDto()
        private val method = HttpMethod.PUT
        private val uri = "$PersonApiEndpoint/${person.id}"

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
        fun `should return 400 if params are too short`() = authenticated(adminPrincipal) {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(
                    updateDto.copy(
                        lastName = "",
                        firstName = ""
                    )
                ),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(
                        sizeErrorDto("lastName", PersonLastNameMinSize, PersonLastNameMaxSize),
                        sizeErrorDto("firstName", PersonFirstNameMinSize, PersonFirstNameMaxSize)
                    )
                )
            )
        }

        @Test
        fun `should return 400 if name is too long`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(
                    updateDto.copy(
                        lastName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        firstName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                    )
                ),
                status = HttpStatus.BAD_REQUEST,
                respDto = ErrorDto.ValidationError(
                    errors = listOf(
                        sizeErrorDto("lastName", PersonLastNameMinSize, PersonLastNameMaxSize),
                        sizeErrorDto("firstName", PersonFirstNameMinSize, PersonFirstNameMaxSize)
                    )
                )
            )
        }

        @Test
        fun `should return 409 if person already exists`() = authenticated(adminPrincipal) {
            whenever(personService.update(person.id, eventContent, source)).thenAnswer {
                throw PersonAlreadyExistsException(person)
            }
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(updateDto),
                status = HttpStatus.CONFLICT,
                respDto = ErrorDto.DuplicateResource(URI("$PersonApiEndpoint/${person.id}"))
            )
            verify(personService).update(person.id, eventContent, source)
        }

        @Test
        fun `should return 403 if person is not admin or super admin`() = authenticated {
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(updateDto),
                status = HttpStatus.FORBIDDEN,
                respDto = ErrorDto.Forbidden
            )
        }

        @Test
        fun `should return 200 (admin)`() = authenticated(adminPrincipal) {
            whenever(personService.update(person.id, eventContent, source)).thenReturn(person)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(updateDto),
                status = HttpStatus.OK,
                respDto = personDto
            )
            verify(personService).update(person.id, eventContent, source)
        }

        @Test
        fun `should return 200 (super_admin)`() = authenticated(superAdminPrincipal) {
            whenever(personService.update(person.id, eventContent, source)).thenReturn(person)
            callAndExpectDto(
                method = method,
                uri = uri,
                reqCookies = listOf(accessTokenCookie()),
                reqContent = mapper.writeValueAsString(updateDto),
                status = HttpStatus.OK,
                respDto = personDto
            )
            verify(personService).update(person.id, eventContent, source)
        }
    }
}
