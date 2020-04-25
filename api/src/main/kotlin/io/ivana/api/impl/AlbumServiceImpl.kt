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

    override fun getAllPhotos(id: UUID, pageNo: Int, pageSize: Int): Page<Photo> {
        val content = photoRepo.fetchAllOfAlbum(id, (pageNo - 1) * pageSize, pageSize)
        val itemsNb = photoRepo.countOfAlbum(id)
        return Page(
            content = content,
            no = pageNo,
            totalItems = itemsNb,
            totalPages = ceil(itemsNb.toDouble() / pageSize.toDouble()).toInt()
        )
    }
}
