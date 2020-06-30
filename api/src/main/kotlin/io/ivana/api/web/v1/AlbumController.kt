package io.ivana.api.web.v1

import io.ivana.api.impl.ForbiddenException
import io.ivana.api.impl.UnknownPermissionException
import io.ivana.api.security.AlbumTargetType
import io.ivana.api.security.UserPrincipal
import io.ivana.api.web.remoteHost
import io.ivana.api.web.source
import io.ivana.core.*
import io.ivana.dto.*
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
@RequestMapping(AlbumApiEndpoint)
@Validated
class AlbumController(
    private val albumService: AlbumService,
    private val userService: UserService,
    private val photoService: PhotoService
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
        return albumService.create(creationDto.name, req.source(principal)).toLightDto()
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
    fun get(@PathVariable id: UUID, auth: Authentication): AlbumDto.Complete {
        val principal = auth.principal as UserPrincipal
        val perms = albumService.getPermissions(id, principal.user.id)
        return albumService.getById(id).toCompleteDto(perms)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAll(
        @RequestParam(name = PageParamName, required = false, defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = SizeParamName, required = false, defaultValue = "10") @Min(1) size: Int,
        auth: Authentication
    ): PageDto<AlbumDto.Light> {
        val principal = auth.principal as UserPrincipal
        return albumService.getAll(principal.user.id, page, size).toDto { it.toLightDto() }
    }

    @GetMapping("/{id:$UuidRegex}$ContentEndpoint")
    @PreAuthorize("hasPermission(#id, '$AlbumTargetType', 'read')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun getAllPhotos(
        @PathVariable id: UUID,
        @RequestParam(name = PageParamName, required = false, defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = SizeParamName, required = false, defaultValue = "10") @Min(1) size: Int,
        auth: Authentication
    ): PageDto<PhotoDto.Light> {
        val principal = auth.principal as UserPrincipal
        return albumService.getAllPhotos(id, principal.user.id, page, size).toDto { it.toLightDto() }
    }

    @GetMapping("/{id:$UuidRegex}$PermissionsEndpoint")
    @PreAuthorize("hasPermission(#id, '$AlbumTargetType', 'update_permissions')")
    @ResponseStatus(HttpStatus.OK)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun getPermissions(
        @PathVariable id: UUID,
        @RequestParam(name = PageParamName, required = false, defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = SizeParamName, required = false, defaultValue = "10") @Min(1) size: Int
    ): PageDto<SubjectPermissionsDto> {
        val subjPerms = albumService.getAllPermissions(id, page, size)
        val usersIds = subjPerms.content.map { it.subjectId }.toSet()
        val users = userService.getAllByIds(usersIds).map { it.id to it }.toMap()
        return subjPerms.toDto { it.toDto(users.getValue(it.subjectId).name) }
    }

    @GetMapping(SharedEndpoint)
    @ResponseStatus(HttpStatus.OK)
    fun getShared(
        @RequestParam(name = PageParamName, required = false, defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = SizeParamName, required = false, defaultValue = "10") @Min(1) size: Int,
        auth: Authentication
    ): PageDto<AlbumDto> {
        val principal = auth.principal as UserPrincipal
        return albumService.getShared(principal.user.id, page, size).toDto { it.toLightDto() }
    }

    @GetMapping(SuggestEndpoint)
    @ResponseStatus(HttpStatus.OK)
    fun suggest(
        @RequestParam(name = QParamName) @NotBlank q: String,
        @RequestParam(name = PermParamName, required = false, defaultValue = "read") @NotBlank permLabel: String,
        @RequestParam(name = CountParamName, required = false, defaultValue = "5") @Min(1) count: Int,
        auth: Authentication
    ): List<AlbumDto.Light> {
        val principal = auth.principal as UserPrincipal
        try {
            val perm = Permission.fromLabel(permLabel)
            return albumService.suggest(q.trim(), count, principal.user.id, perm).map { it.toLightDto() }
        } catch (exception: IllegalArgumentException) {
            throw UnknownPermissionException(permLabel)
        }
    }

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
        val user = principal.user
        if (!photoService.userCanReadAll(updateDto.photosToAdd.toSet(), user.id)) {
            val remoteAddr = req.remoteHost()
            throw ForbiddenException("User '${user.name}' ($remoteAddr) attempted to add photos without read permission in album $id")
        }
        return albumService.update(id, updateDto.toUpdateContent(), req.source(principal)).toLightDto()
    }

    @Transactional
    @PutMapping("/{id:$UuidRegex}$PermissionsEndpoint")
    @PreAuthorize("hasPermission(#id, '$AlbumTargetType', 'update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Suppress("MVCPathVariableInspection", "RegExpUnexpectedAnchor")
    fun updatePermissions(
        @PathVariable id: UUID,
        @RequestBody @Valid dto: UpdatePermissionsDto,
        auth: Authentication,
        req: HttpServletRequest
    ) {
        val principal = auth.principal as UserPrincipal
        val usersIds = (dto.permissionsToAdd + dto.permissionsToRemove).map { it.subjectId }.toSet()
        val users = userService.getAllByIds(usersIds).map { it.id to it }.toMap()
        albumService.updatePermissions(
            id = id,
            permissionsToAdd = dto.permissionsToAdd.toUserPermissionsSet(users),
            permissionsToRemove = dto.permissionsToRemove.toUserPermissionsSet(users),
            source = req.source(principal)
        )
    }

    private fun AlbumUpdateDto.toUpdateContent() = AlbumEvent.Update.Content(
        name = name,
        photosToAdd = photosToAdd.distinct(),
        photosToRemove = photosToRemove.distinct()
    )
}
