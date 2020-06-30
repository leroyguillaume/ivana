package io.ivana.core

import java.io.File
import java.io.InputStream
import java.time.LocalDate
import java.util.*

interface PhotoService : OwnableEntityService<Photo> {
    fun delete(id: UUID, source: EventSource.User)

    fun getCompressedFile(photo: Photo): File

    fun getLinkedById(id: UUID): LinkedPhotos

    fun getLinkedById(id: UUID, userId: UUID): LinkedPhotos

    fun getLinkedById(id: UUID, userId: UUID, albumId: UUID): LinkedPhotos

    fun getPeople(id: UUID): List<Person>

    fun getRawFile(photo: Photo): File

    fun transform(id: UUID, transform: Transform, source: EventSource.User)

    fun update(id: UUID, shootingDate: LocalDate?, source: EventSource.User): Photo

    fun updatePeople(id: UUID, peopleToAdd: Set<Person>, peopleToRemove: Set<Person>, source: EventSource.User)

    fun updatePermissions(
        id: UUID,
        permissionsToAdd: Set<UserPermissions>,
        permissionsToRemove: Set<UserPermissions>,
        source: EventSource.User
    )

    fun upload(input: InputStream, type: Photo.Type, source: EventSource.User): Photo

    fun userCanReadAll(ids: Set<UUID>, userId: UUID): Boolean
}
