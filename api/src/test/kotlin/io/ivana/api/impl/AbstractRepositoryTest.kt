package io.ivana.api.impl

import io.ivana.core.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.net.InetAddress
import java.util.*

internal abstract class AbstractRepositoryTest {
    private companion object {
        val HashRegex = Regex("hash([0-9]+)")
    }

    @Autowired
    protected lateinit var jdbc: NamedParameterJdbcTemplate

    @Autowired
    protected lateinit var userEventRepo: UserEventRepository

    @Autowired
    protected lateinit var userRepo: UserRepository

    @Autowired
    protected lateinit var photoEventRepo: PhotoEventRepository

    @Autowired
    protected lateinit var photoRepo: PhotoRepository

    @Autowired
    protected lateinit var userPhotoAuthzRepo: UserPhotoAuthorizationRepository

    @Autowired
    protected lateinit var albumEventRepo: AlbumEventRepository

    @Autowired
    protected lateinit var albumRepo: AlbumRepository

    @Autowired
    protected lateinit var userAlbumAuthzRepo: UserAlbumAuthorizationRepository

    // 3 users
    protected lateinit var userCreationEvents: List<UserEvent.Creation>

    // 9 photos (3 by user)
    // User 1 can read photo 8
    // User 2 can read photo 1
    // User 2 can't read photo 2
    protected lateinit var photoUploadEvents: List<PhotoEvent.Upload>
    protected lateinit var photoUpdatePermissionsEvents: List<PhotoEvent.UpdatePermissions>

    // 9 albums (3 by user)
    // Album 1 contains all photos of user 1
    // User 2 can read album 1
    protected lateinit var albumCreationEvents: List<AlbumEvent.Creation>
    protected lateinit var albumUpdateEvents: List<AlbumEvent.Update>
    protected lateinit var albumUpdatePermissionsEvents: List<AlbumEvent.UpdatePermissions>

    @BeforeEach
    fun beforeEach() {
        cleanDb()
        initUsers()
        initPhotos()
        initAlbums()
    }

    protected fun nextAlbumEventNumber() = nextEventNumber(AlbumEventRepositoryImpl.TableName)

    protected fun nextPhotoEventNumber() = nextEventNumber(PhotoEventRepositoryImpl.TableName)

    protected fun nextUserEventNumber() = nextEventNumber(UserEventRepositoryImpl.TableName)

    protected fun userLocalSource(userId: UUID) = EventSource.User(userId, InetAddress.getByName("127.0.0.1"))

    private fun cleanDb() {
        jdbc.deleteAllOfTables(
            UserEventRepositoryImpl.TableName,
            PhotoEventRepositoryImpl.TableName,
            AlbumEventRepositoryImpl.TableName,
            UserRepositoryImpl.TableName
        )
        jdbc.resetEventNumberSequence(
            UserEventRepositoryImpl.TableName,
            PhotoEventRepositoryImpl.TableName,
            AlbumEventRepositoryImpl.TableName
        )
        jdbc.update(
            "ALTER SEQUENCE ${PhotoRepositoryImpl.TableName}_${PhotoRepositoryImpl.NoColumnName}_seq RESTART",
            MapSqlParameterSource()
        )
    }

    private fun addPermissionsOnAlbum(
        albumCreationEvent: AlbumEvent.Creation,
        userId: UUID,
        vararg permissions: Permission
    ) = albumEventRepo.saveUpdatePermissionsEvent(
        albumId = albumCreationEvent.subjectId,
        content = AlbumEvent.UpdatePermissions.Content(
            permissionsToAdd = setOf(
                SubjectPermissions(
                    subjectId = userId,
                    permissions = permissions.toSet()
                )
            ),
            permissionsToRemove = emptySet()
        ),
        source = albumCreationEvent.source
    )

    private fun addPermissionsOnPhoto(
        photoUploadEvent: PhotoEvent.Upload,
        userId: UUID,
        vararg permissions: Permission
    ) = photoEventRepo.saveUpdatePermissionsEvent(
        photoId = photoUploadEvent.subjectId,
        content = PhotoEvent.UpdatePermissions.Content(
            permissionsToAdd = setOf(
                SubjectPermissions(
                    subjectId = userId,
                    permissions = permissions.toSet()
                )
            ),
            permissionsToRemove = emptySet()
        ),
        source = photoUploadEvent.source
    )

    private fun addPhotosToAlbum(albumCreationEvent: AlbumEvent.Creation, photosIds: List<UUID>) =
        albumEventRepo.saveUpdateEvent(
            albumId = albumCreationEvent.subjectId,
            content = AlbumEvent.Update.Content(
                name = albumCreationEvent.albumName,
                photosToAdd = photosIds,
                photosToRemove = emptyList()
            ),
            source = albumCreationEvent.source
        )

    private fun initAlbums() {
        var i = 1
        albumCreationEvents = userCreationEvents
            .map { userCreationEvent ->
                (0..2).map {
                    albumEventRepo.saveCreationEvent("album${i++}", userLocalSource(userCreationEvent.subjectId))
                }
            }
            .flatten()
        val album1UpdateEvent = albumCreationEvents[0].let { albumCreationEvent ->
            addPhotosToAlbum(
                albumCreationEvent = albumCreationEvent,
                photosIds = photoUploadEvents
                    .filter { it.source.id == albumCreationEvent.source.id }
                    .map { it.subjectId }
            )
        }
        albumUpdateEvents = listOf(album1UpdateEvent)

        val album1UpdatePermissionsEvent = addPermissionsOnAlbum(
            albumCreationEvent = albumCreationEvents[0],
            userId = userCreationEvents[1].subjectId,
            permissions = *arrayOf(Permission.Read)
        )
        albumUpdatePermissionsEvents = listOf(album1UpdatePermissionsEvent)
    }

    private fun initPhotos() {
        var i = 1
        photoUploadEvents = userCreationEvents
            .map { userCreationEvent ->
                (0..2).map {
                    photoEventRepo.saveUploadEvent(
                        content = PhotoEvent.Upload.Content(
                            type = Photo.Type.Jpg,
                            hash = "hash${i++}" // Trick here to get no
                        ),
                        source = userLocalSource(userCreationEvent.subjectId)
                    )
                }
            }
            .flatten()

        val photo1UpdatePermissionsEvent = addPermissionsOnPhoto(
            photoUploadEvent = photoUploadEvents[0],
            userId = userCreationEvents[1].subjectId,
            permissions = *arrayOf(Permission.Read)
        )
        val photo2UpdatePermissionsEvent = removePermissionsOnPhoto(
            photoUploadEvent = photoUploadEvents[1],
            userId = userCreationEvents[1].subjectId,
            permissions = *arrayOf(Permission.Read)
        )
        val photo8UpdatePermissionsEvent = addPermissionsOnPhoto(
            photoUploadEvent = photoUploadEvents[7],
            userId = userCreationEvents[0].subjectId,
            permissions = *arrayOf(Permission.Read)
        )
        photoUpdatePermissionsEvents = listOf(
            photo1UpdatePermissionsEvent,
            photo2UpdatePermissionsEvent,
            photo8UpdatePermissionsEvent
        )
    }

    private fun initUsers() {
        userCreationEvents = listOf(
            UserEvent.Creation.Content("user1", "hashedPwd", Role.SuperAdmin),
            UserEvent.Creation.Content("user2", "hashedPwd", Role.Admin),
            UserEvent.Creation.Content("user3", "hashedPwd", Role.User)
        ).map { userEventRepo.saveCreationEvent(it, EventSource.System) }
    }

    private fun nextEventNumber(tableName: String) = jdbc.queryForObject(
        "SELECT currval('${tableName}_${AbstractEventRepository.NumberColumnName}_seq')",
        MapSqlParameterSource()
    ) { rs, _ -> rs.getLong(1) + 1 }!!

    private fun removePermissionsOnPhoto(
        photoUploadEvent: PhotoEvent.Upload,
        userId: UUID,
        vararg permissions: Permission
    ) = photoEventRepo.saveUpdatePermissionsEvent(
        photoId = photoUploadEvent.subjectId,
        content = PhotoEvent.UpdatePermissions.Content(
            permissionsToAdd = emptySet(),
            permissionsToRemove = setOf(
                SubjectPermissions(
                    subjectId = userId,
                    permissions = permissions.toSet()
                )
            )
        ),
        source = photoUploadEvent.source
    )

    protected fun AlbumEvent.Creation.toAlbum() = Album(
        id = subjectId,
        ownerId = source.id,
        name = albumName,
        creationDate = date
    )

    protected fun PhotoEvent.Upload.toPhoto(version: Int = 1) = Photo(
        id = subjectId,
        ownerId = source.id,
        uploadDate = date,
        type = content.type,
        hash = content.hash,
        no = HashRegex.matchEntire(content.hash)!!.groupValues[1].toInt(),
        version = version
    )

    protected fun UserEvent.Creation.toUser() = User(
        id = subjectId,
        name = content.name,
        hashedPwd = content.hashedPwd,
        role = content.role,
        creationDate = date
    )

    @Suppress("SqlWithoutWhere")
    private fun NamedParameterJdbcTemplate.deleteAllOfTables(vararg tableNames: String) {
        tableNames.forEach { update("DELETE FROM $it", MapSqlParameterSource()) }
    }

    @Suppress("SqlResolve")
    private fun NamedParameterJdbcTemplate.resetEventNumberSequence(vararg tableNames: String) {
        tableNames.forEach { tableName ->
            update(
                "ALTER SEQUENCE ${tableName}_${AbstractEventRepository.NumberColumnName}_seq RESTART",
                MapSqlParameterSource()
            )
        }
    }
}
