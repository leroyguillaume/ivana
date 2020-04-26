package io.ivana.api.web.v1

import io.ivana.api.security.AlbumTargetType
import io.ivana.api.security.UserPrincipal
import io.ivana.api.web.source
import io.ivana.core.AlbumEvent
import io.ivana.core.AlbumService
import io.ivana.dto.AlbumCreationDto
import io.ivana.dto.AlbumDto
import io.ivana.dto.AlbumUpdateDto
import io.ivana.dto.PageDto
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

@RestController
@RequestMapping(AlbumApiEndpoint)
@Validated
class AlbumController(
    private val albumService: AlbumService
) {
    @Transactional
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody @Valid creationDto: AlbumCreationDto,
        auth: Authentication,
        req: HttpServletRequest
    ): AlbumDto {
        val principal = auth.principal as UserPrincipal
        return albumService.create(creationDto.name, req.source(principal)).toDto()
    }

    @Transactional
    @DeleteMapping("/{id:$UuidRegex}")
    @PreAuthorize("hasPermission(#id, '$AlbumTargetType', 'delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun delete(@PathVariable id: UUID, auth: Authentication, req: HttpServletRequest) {
        val principal = auth.principal as UserPrincipal
        albumService.delete(id, req.source(principal))
    }

    @GetMapping("/{id:$UuidRegex}")
    @PreAuthorize("hasPermission(#id, '$AlbumTargetType', 'read')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun get(@PathVariable id: UUID) = albumService.getById(id).toDto()

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAll(
        @RequestParam(name = PageParamName, required = false, defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = SizeParamName, required = false, defaultValue = "10") @Min(1) size: Int,
        auth: Authentication
    ): PageDto<AlbumDto> {
        val principal = auth.principal as UserPrincipal
        return albumService.getAll(principal.user.id, page, size).toDto { it.toDto() }
    }

    @GetMapping("/{id:$UuidRegex}$ContentEndpoint")
    @PreAuthorize("hasPermission(#id, '$AlbumTargetType', 'read')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun getAllPhotos(
        @PathVariable id: UUID,
        @RequestParam(name = PageParamName, required = false, defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = SizeParamName, required = false, defaultValue = "10") @Min(1) size: Int
    ) = albumService.getAllPhotos(id, page, size).toDto { it.toSimpleDto() }

    @Transactional
    @PutMapping("/{id:$UuidRegex}")
    @PreAuthorize("hasPermission(#id, '$AlbumTargetType', 'update')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun update(
        @PathVariable id: UUID,
        @RequestBody @Valid updateDto: AlbumUpdateDto,
        auth: Authentication,
        req: HttpServletRequest
    ): AlbumDto {
        val principal = auth.principal as UserPrincipal
        return albumService.update(id, updateDto.toUpdateContent(), req.source(principal)).toDto()
    }

    private fun AlbumUpdateDto.toUpdateContent() = AlbumEvent.Update.Content(
        name = name,
        photosToAdd = photosToAdd.distinct(),
        photosToRemove = photosToRemove.distinct()
    )
}
