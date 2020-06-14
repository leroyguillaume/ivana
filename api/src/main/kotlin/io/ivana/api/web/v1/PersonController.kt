package io.ivana.api.web.v1

import io.ivana.api.security.UserPrincipal
import io.ivana.api.web.source
import io.ivana.core.PersonEvent
import io.ivana.core.PersonService
import io.ivana.dto.PageDto
import io.ivana.dto.PersonCreationDto
import io.ivana.dto.PersonDto
import io.ivana.dto.PersonUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping(PersonApiEndpoint)
@Validated
class PersonController(
    private val personService: PersonService
) {
    @Transactional
    @PostMapping
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody @Valid creationDto: PersonCreationDto,
        auth: Authentication,
        req: HttpServletRequest
    ): PersonDto {
        val principal = auth.principal as UserPrincipal
        val content = PersonEvent.Creation.Content(
            firstName = creationDto.firstName,
            lastName = creationDto.lastName
        )
        return personService.create(content, req.source(principal)).toDto()
    }

    @Transactional
    @DeleteMapping("/{id:$UuidRegex}")
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun delete(@PathVariable id: UUID, auth: Authentication, req: HttpServletRequest) {
        val principal = auth.principal as UserPrincipal
        personService.delete(id, req.source(principal))
    }

    @GetMapping("/{id:$UuidRegex}")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun get(@PathVariable id: UUID) = personService.getById(id).toDto()

    @GetMapping
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @ResponseStatus(HttpStatus.OK)
    fun getAll(
        @RequestParam(name = PageParamName, required = false, defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = SizeParamName, required = false, defaultValue = "10") @Min(1) size: Int,
        auth: Authentication
    ): PageDto<PersonDto> {
        return personService.getAll(page, size).toDto { it.toDto() }
    }

    @GetMapping(SuggestEndpoint)
    @ResponseStatus(HttpStatus.OK)
    fun suggest(
        @RequestParam(name = QParamName) @NotBlank q: String,
        @RequestParam(name = CountParamName, required = false, defaultValue = "5") @Min(1) count: Int
    ) = personService.suggest(q.trim(), count).map { it.toDto() }

    @Transactional
    @PutMapping("/{id:$UuidRegex}")
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun update(
        @PathVariable id: UUID,
        @RequestBody @Valid dto: PersonUpdateDto,
        auth: Authentication,
        req: HttpServletRequest
    ): PersonDto {
        val principal = auth.principal as UserPrincipal
        val content = PersonEvent.Update.Content(
            lastName = dto.lastName,
            firstName = dto.firstName
        )
        val person = personService.update(id, content, req.source(principal))
        return person.toDto()
    }
}
