package io.ivana.api.impl

import io.ivana.core.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.math.ceil

@Service
class AlbumServiceImpl(
    override val repo: AlbumRepository,
    override val userRepo: UserRepository,
    override val authzRepo: UserAlbumAuthorizationRepository,
    private val eventRepo: AlbumEventRepository,
    private val photoRepo: PhotoRepository
) : AlbumService, AbstractOwnableEntityService<Album>() {
    private companion object {
        val Logger = LoggerFactory.getLogger(AlbumServiceImpl::class.java)
    }

    override val entityName = "Album"

    @Transactional
    override fun create(name: String, source: EventSource.User): Album {
        val event = eventRepo.saveCreationEvent(name, source)
        Logger.info("User ${source.id} (${source.ip}) created album '$name' (${event.subjectId})")
        return repo.fetchById(event.subjectId)!!
    }

    override fun getAllPhotos(id: UUID, userId: UUID, pageNo: Int, pageSize: Int): Page<Photo> {
        val itemsNb = repo.fetchSize(id, userId) ?: throw EntityNotFoundException("Album $id does not exist")
        val content = photoRepo.fetchAllOfAlbum(id, userId, (pageNo - 1) * pageSize, pageSize)
        return Page(
            content = content,
            no = pageNo,
            totalItems = itemsNb,
            totalPages = ceil(itemsNb.toDouble() / pageSize.toDouble()).toInt()
        )
    }

    @Transactional
    override fun delete(id: UUID, source: EventSource.User) {
        if (!repo.existsById(id)) {
            throw EntityNotFoundException("$entityName $id does not exist")
        }
        eventRepo.saveDeletionEvent(id, source)
        Logger.info("User ${source.id} (${source.ip}) deleted album $id")
    }

    @Transactional
    override fun update(id: UUID, content: AlbumEvent.Update.Content, source: EventSource.User): Album {
        if (!repo.existsById(id)) {
            throw EntityNotFoundException("$entityName $id does not exist")
        }
        val photosToAddIds = content.photosToAdd.toSet()
        if (photosToAddIds.isNotEmpty()) {
            val existingIds = photoRepo.fetchExistingIds(photosToAddIds)
            val notFoundIds = photosToAddIds - existingIds
            if (notFoundIds.isNotEmpty()) {
                throw ResourcesNotFoundException.Photo(notFoundIds)
            }
            val duplicateIds = repo.fetchDuplicateIds(id, photosToAddIds)
            if (duplicateIds.isNotEmpty()) {
                throw AlbumAlreadyContainsPhotosException(duplicateIds)
            }
        }
        eventRepo.saveUpdateEvent(id, content, source)
        Logger.info("User ${source.id} (${source.ip}) updated album $id")
        return getById(id)
    }

    @Transactional
    override fun updatePermissions(
        id: UUID,
        permissionsToAdd: Set<UserPermissions>,
        permissionsToRemove: Set<UserPermissions>,
        source: EventSource.User
    ) {
        val album = getById(id)
        if (permissionsToRemove.find { it.user.id == album.ownerId } != null) {
            throw OwnerPermissionsUpdateException()
        }
        val content = AlbumEvent.UpdatePermissions.Content(
            permissionsToAdd = permissionsToAdd.toSubjectPermissionsSet(),
            permissionsToRemove = permissionsToRemove.toSubjectPermissionsSet()
        )
        eventRepo.saveUpdatePermissionsEvent(id, content, source)
        Logger.info("User ${source.id} (${source.ip}) updated permissions of album $id")
    }

    override fun throwResourcesNotFoundException(ids: Set<UUID>) {
        throw ResourcesNotFoundException.Album(ids)
    }
}
