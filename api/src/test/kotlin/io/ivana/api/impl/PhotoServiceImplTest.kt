@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.api.config.IvanaProperties
import io.ivana.core.*
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.matchers.file.shouldNotExist
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.*
import java.io.File
import java.net.InetAddress
import java.nio.file.Files
import java.time.OffsetDateTime
import java.util.*

internal class PhotoServiceImplTest {
    private companion object {
        private val Props = IvanaProperties(
            dataDir = Files.createTempDirectory("ivana").toFile(),
            compressionQuality = 0.3F
        )

        @AfterAll
        @JvmStatic
        fun afterAll() {
            Props.dataDir.deleteRecursively()
        }
    }

    private lateinit var photoEventRepo: PhotoEventRepository
    private lateinit var authzRepo: UserPhotoAuthorizationRepository
    private lateinit var photoRepo: PhotoRepository
    private lateinit var userRepo: UserRepository
    private lateinit var albumRepo: AlbumRepository
    private lateinit var service: PhotoServiceImpl

    @BeforeEach
    fun beforeEach() {
        photoEventRepo = mockk()
        authzRepo = mockk()
        photoRepo = mockk()
        userRepo = mockk()
        albumRepo = mockk()

        service = PhotoServiceImpl(photoRepo, userRepo, authzRepo, photoEventRepo, albumRepo, Props)
    }

    @Nested
    inner class delete {
        private val photo = Photo(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1,
            version = 2
        )
        private val event = PhotoEvent.Deletion(
            date = OffsetDateTime.now(),
            subjectId = photo.id,
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1"))
        )
        private val file = File(javaClass.getResource("/data/photo.jpg").file)

        @BeforeEach
        fun beforeEach() {
            copyPhoto(photo, file, 2)
            copyPhoto(photo, file, 3)
        }

        @Test
        fun `should throw exception if photo does not exist`() {
            every { photoRepo.fetchById(event.subjectId) } returns null
            val exception = assertThrows<EntityNotFoundException> { service.delete(event.subjectId, event.source) }
            exception shouldHaveMessage "Photo ${event.subjectId} does not exist"
            verify { photoRepo.fetchById(event.subjectId) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should delete photo`() {
            every { photoRepo.fetchById(event.subjectId) } returns photo
            every { photoEventRepo.saveDeletionEvent(event.subjectId, event.source) } returns event
            service.delete(event.subjectId, event.source)
            rawFile(photo.id, photo.uploadDate, photo.type.extension(), 2).shouldNotExist()
            rawFile(photo.id, photo.uploadDate, photo.type.extension(), 3).shouldNotExist()
            compressedFile(photo.id, photo.uploadDate, photo.type.extension(), 2).shouldNotExist()
            compressedFile(photo.id, photo.uploadDate, photo.type.extension(), 3).shouldNotExist()
            verify { photoRepo.fetchById(event.subjectId) }
            verify { photoEventRepo.saveDeletionEvent(event.subjectId, event.source) }
            confirmVerified(photoRepo)
        }
    }

    @Nested
    inner class getAll {
        private val pageNo = 1
        private val pageSize = 3
        private val expectedPage = Page(
            content = listOf(
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = UUID.randomUUID(),
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash1",
                    no = 1,
                    version = 1
                ),
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = UUID.randomUUID(),
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash2",
                    no = 2,
                    version = 1
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )

        @Test
        fun `should return page`() {
            every { photoRepo.fetchAll(pageNo - 1, pageSize) } returns expectedPage.content
            every { photoRepo.count() } returns expectedPage.totalItems
            val page = service.getAll(pageNo, pageSize)
            page shouldBe expectedPage
            verify { photoRepo.fetchAll(pageNo - 1, pageSize) }
            verify { photoRepo.count() }
            confirmVerified(photoRepo)
        }
    }

    @Nested
    inner class `getAll with owner id` {
        private val ownerId = UUID.randomUUID()
        private val pageNo = 1
        private val pageSize = 3
        private val expectedPage = Page(
            content = listOf(
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = ownerId,
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash1",
                    no = 1,
                    version = 1
                ),
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = ownerId,
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash2",
                    no = 2,
                    version = 1
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )

        @Test
        fun `should return page`() {
            every { photoRepo.fetchAll(ownerId, pageNo - 1, pageSize) } returns expectedPage.content
            every { photoRepo.count(ownerId) } returns expectedPage.totalItems
            val page = service.getAll(ownerId, pageNo, pageSize)
            page shouldBe expectedPage
            verify { photoRepo.fetchAll(ownerId, pageNo - 1, pageSize) }
            verify { photoRepo.count(ownerId) }
            confirmVerified(photoRepo)
        }
    }

    @Nested
    inner class getAllByIds {
        private val expectedPhotos = setOf(
            Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash1",
                no = 1,
                version = 1
            ),
            Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash2",
                no = 2,
                version = 1
            )
        )
        private val ids = expectedPhotos.map { it.id }.toSet()

        @Test
        fun `should throw exception if entities not found`() {
            every { photoRepo.fetchAllByIds(ids) } returns emptySet()
            val exception = assertThrows<ResourcesNotFoundException> { service.getAllByIds(ids) }
            exception.ids shouldBe ids
            verify { photoRepo.fetchAllByIds(ids) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return all photos`() {
            every { photoRepo.fetchAllByIds(ids) } returns expectedPhotos
            val photos = service.getAllByIds(ids)
            photos shouldBe expectedPhotos
            verify { photoRepo.fetchAllByIds(ids) }
            confirmVerified(photoRepo)
        }
    }

    @Nested
    inner class getById {
        private val expectedPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1,
            version = 1
        )

        @Test
        fun `should throw exception if photo does not exist`() {
            every { photoRepo.fetchById(expectedPhoto.id) } returns null
            val exception = assertThrows<EntityNotFoundException> { service.getById(expectedPhoto.id) }
            exception shouldHaveMessage "Photo ${expectedPhoto.id} does not exist"
            verify { photoRepo.fetchById(expectedPhoto.id) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return photo with id`() {
            every { photoRepo.fetchById(expectedPhoto.id) } returns expectedPhoto
            val photo = service.getById(expectedPhoto.id)
            photo shouldBe expectedPhoto
            verify { photoRepo.fetchById(photo.id) }
            confirmVerified(photoRepo)
        }
    }

    @Nested
    inner class getCompressedFile {
        private val photo = Photo(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1,
            version = 1
        )
        private val expectedFile = compressedFile(photo.id, photo.uploadDate, "jpg")

        @Test
        fun `should return file`() {
            val file = service.getCompressedFile(photo)
            file shouldBe expectedFile
        }
    }

    @Nested
    inner class getRawFile {
        private val photo = Photo(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1,
            version = 1
        )
        private val expectedFile = rawFile(photo.id, photo.uploadDate, "jpg")

        @Test
        fun `should return file`() {
            val file = service.getRawFile(photo)
            file shouldBe expectedFile
        }
    }

    @Nested
    inner class getLinkedById {
        private val defaultLinkedPhotos = LinkedPhotos(
            current = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 2,
                version = 1
            )
        )
        private val completeLinkedPhotos = defaultLinkedPhotos.copy(
            next = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 1,
                version = 1
            ),
            previous = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 3,
                version = 1
            )
        )

        @Test
        fun `should throw exception if photo does not exist`() {
            every { photoRepo.fetchById(defaultLinkedPhotos.current.id) } returns null
            val exception = assertThrows<EntityNotFoundException> {
                service.getLinkedById(defaultLinkedPhotos.current.id)
            }
            exception shouldHaveMessage "Photo ${defaultLinkedPhotos.current.id} does not exist"
            verify { photoRepo.fetchById(defaultLinkedPhotos.current.id) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return default linked photos`() {
            every { photoRepo.fetchById(defaultLinkedPhotos.current.id) } returns defaultLinkedPhotos.current
            every { photoRepo.fetchPreviousOf(defaultLinkedPhotos.current.no) } returns null
            every { photoRepo.fetchNextOf(defaultLinkedPhotos.current.no) } returns null
            val linkedPhotos = service.getLinkedById(defaultLinkedPhotos.current.id)
            linkedPhotos shouldBe defaultLinkedPhotos
            verify { photoRepo.fetchById(defaultLinkedPhotos.current.id) }
            verify { photoRepo.fetchPreviousOf(defaultLinkedPhotos.current.no) }
            verify { photoRepo.fetchNextOf(defaultLinkedPhotos.current.no) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return complete linked photos`() {
            every { photoRepo.fetchById(completeLinkedPhotos.current.id) } returns completeLinkedPhotos.current
            every {
                photoRepo.fetchPreviousOf(completeLinkedPhotos.current.no)
            } returns completeLinkedPhotos.previous
            every {
                photoRepo.fetchNextOf(completeLinkedPhotos.current.no)
            } returns completeLinkedPhotos.next
            val linkedPhotos = service.getLinkedById(completeLinkedPhotos.current.id)
            linkedPhotos shouldBe completeLinkedPhotos
            verify { photoRepo.fetchById(completeLinkedPhotos.current.id) }
            verify { photoRepo.fetchPreviousOf(completeLinkedPhotos.current.no) }
            verify { photoRepo.fetchNextOf(defaultLinkedPhotos.current.no) }
            confirmVerified(photoRepo)
        }
    }

    @Nested
    inner class `getLinkedById with user id` {
        private val userId = UUID.randomUUID()
        private val defaultLinkedPhotos = LinkedPhotos(
            current = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 2,
                version = 1
            )
        )
        private val completeLinkedPhotos = defaultLinkedPhotos.copy(
            next = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 1,
                version = 1
            ),
            previous = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 3,
                version = 1
            )
        )

        @Test
        fun `should throw exception if photo does not exist`() {
            every { photoRepo.fetchById(defaultLinkedPhotos.current.id) } returns null
            val exception = assertThrows<EntityNotFoundException> {
                service.getLinkedById(defaultLinkedPhotos.current.id, userId)
            }
            exception shouldHaveMessage "Photo ${defaultLinkedPhotos.current.id} does not exist"
            verify { photoRepo.fetchById(defaultLinkedPhotos.current.id) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return default linked photos`() {
            every { photoRepo.fetchById(defaultLinkedPhotos.current.id) } returns defaultLinkedPhotos.current
            every { photoRepo.fetchPreviousOf(defaultLinkedPhotos.current.no, userId) } returns null
            every { photoRepo.fetchNextOf(defaultLinkedPhotos.current.no, userId) } returns null
            val linkedPhotos = service.getLinkedById(defaultLinkedPhotos.current.id, userId)
            linkedPhotos shouldBe defaultLinkedPhotos
            verify { photoRepo.fetchById(defaultLinkedPhotos.current.id) }
            verify { photoRepo.fetchPreviousOf(defaultLinkedPhotos.current.no, userId) }
            verify { photoRepo.fetchNextOf(defaultLinkedPhotos.current.no, userId) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return complete linked photos`() {
            every { photoRepo.fetchById(completeLinkedPhotos.current.id) } returns completeLinkedPhotos.current
            every {
                photoRepo.fetchPreviousOf(completeLinkedPhotos.current.no, userId)
            } returns completeLinkedPhotos.previous
            every {
                photoRepo.fetchNextOf(completeLinkedPhotos.current.no, userId)
            } returns completeLinkedPhotos.next
            val linkedPhotos = service.getLinkedById(completeLinkedPhotos.current.id, userId)
            linkedPhotos shouldBe completeLinkedPhotos
            verify { photoRepo.fetchById(completeLinkedPhotos.current.id) }
            verify { photoRepo.fetchPreviousOf(completeLinkedPhotos.current.no, userId) }
            verify { photoRepo.fetchNextOf(defaultLinkedPhotos.current.no, userId) }
            confirmVerified(photoRepo)
        }
    }

    @Nested
    inner class `getLinkedById with albumId` {
        private val userId = UUID.randomUUID()
        private val albumId = UUID.randomUUID()
        private val defaultLinkedPhotos = LinkedPhotos(
            current = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 2,
                version = 1
            )
        )
        private val completeLinkedPhotos = defaultLinkedPhotos.copy(
            next = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 1,
                version = 1
            ),
            previous = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 3,
                version = 1
            )
        )
        private val order = 1

        @Test
        fun `should throw exception if photo does not exist`() {
            every { photoRepo.fetchById(defaultLinkedPhotos.current.id) } returns null
            val exception = assertThrows<EntityNotFoundException> {
                service.getLinkedById(defaultLinkedPhotos.current.id, userId, albumId)
            }
            exception shouldHaveMessage "Photo ${defaultLinkedPhotos.current.id} does not exist"
            verify { photoRepo.fetchById(defaultLinkedPhotos.current.id) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should throw exception if photo is not present in album`() {
            every { photoRepo.fetchById(defaultLinkedPhotos.current.id) } returns defaultLinkedPhotos.current
            every { albumRepo.fetchOrder(albumId, defaultLinkedPhotos.current.id) } returns null
            val exception = assertThrows<PhotoNotPresentInAlbumException> {
                service.getLinkedById(defaultLinkedPhotos.current.id, userId, albumId)
            }
            exception shouldHaveMessage "Photo ${defaultLinkedPhotos.current.id} not present in album $albumId"
            verify { photoRepo.fetchById(defaultLinkedPhotos.current.id) }
            verify { albumRepo.fetchOrder(albumId, defaultLinkedPhotos.current.id) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return default linked photos`() {
            every { photoRepo.fetchById(defaultLinkedPhotos.current.id) } returns defaultLinkedPhotos.current
            every { albumRepo.fetchOrder(albumId, defaultLinkedPhotos.current.id) } returns order
            every { photoRepo.fetchPreviousOf(order, userId, albumId) } returns null
            every { photoRepo.fetchNextOf(order, userId, albumId) } returns null
            val linkedPhotos = service.getLinkedById(defaultLinkedPhotos.current.id, userId, albumId)
            linkedPhotos shouldBe defaultLinkedPhotos
            verify { photoRepo.fetchById(defaultLinkedPhotos.current.id) }
            verify { albumRepo.fetchOrder(albumId, defaultLinkedPhotos.current.id) }
            verify { photoRepo.fetchPreviousOf(order, userId, albumId) }
            verify { photoRepo.fetchNextOf(order, userId, albumId) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return complete linked photos`() {
            every { photoRepo.fetchById(completeLinkedPhotos.current.id) } returns completeLinkedPhotos.current
            every { albumRepo.fetchOrder(albumId, defaultLinkedPhotos.current.id) } returns order
            every { photoRepo.fetchPreviousOf(order, userId, albumId) } returns completeLinkedPhotos.previous
            every { photoRepo.fetchNextOf(order, userId, albumId) } returns completeLinkedPhotos.next
            val linkedPhotos = service.getLinkedById(completeLinkedPhotos.current.id, userId, albumId)
            linkedPhotos shouldBe completeLinkedPhotos
            verify { photoRepo.fetchById(completeLinkedPhotos.current.id) }
            verify { albumRepo.fetchOrder(albumId, defaultLinkedPhotos.current.id) }
            verify { photoRepo.fetchPreviousOf(order, userId, albumId) }
            verify { photoRepo.fetchNextOf(order, userId, albumId) }
            confirmVerified(photoRepo)
        }
    }

    @Nested
    inner class getAllPermissions {
        private val photoId = UUID.randomUUID()
        private val pageNo = 1
        private val pageSize = 3
        private val expectedPage = Page(
            content = listOf(
                SubjectPermissions(
                    subjectId = UUID.randomUUID(),
                    permissions = setOf(Permission.Read)
                ),
                SubjectPermissions(
                    subjectId = UUID.randomUUID(),
                    permissions = setOf(Permission.Update)
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )

        @Test
        fun `should return page`() {
            every { authzRepo.fetchAll(photoId, pageNo - 1, pageSize) } returns expectedPage.content
            every { authzRepo.count(photoId) } returns expectedPage.totalItems
            val page = service.getAllPermissions(photoId, pageNo, pageSize)
            page shouldBe expectedPage
            verify { authzRepo.fetchAll(photoId, pageNo - 1, pageSize) }
            verify { authzRepo.count(photoId) }
            confirmVerified(authzRepo)
        }
    }

    @Nested
    inner class getPermissions {
        private val userId = UUID.randomUUID()
        private val photoId = UUID.randomUUID()
        private val permissions = setOf(Permission.Read)

        @Test
        fun `should throw exception if photo does not exist`() {
            every { photoRepo.existsById(photoId) } returns false
            val exception = assertThrows<EntityNotFoundException> { service.getPermissions(photoId, userId) }
            exception shouldHaveMessage "Photo $photoId does not exist"
            verify { photoRepo.existsById(photoId) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should throw exception if user does not exist`() {
            every { photoRepo.existsById(photoId) } returns true
            every { userRepo.existsById(userId) } returns false
            val exception = assertThrows<EntityNotFoundException> { service.getPermissions(photoId, userId) }
            exception shouldHaveMessage "User $userId does not exist"
            verify { photoRepo.existsById(photoId) }
            verify { userRepo.existsById(userId) }
            confirmVerified(photoRepo, userRepo)
        }

        @Test
        fun `should returns empty set if no permissions defined`() {
            every { photoRepo.existsById(photoId) } returns true
            every { userRepo.existsById(userId) } returns true
            every { authzRepo.fetch(userId, photoId) } returns null
            val perms = service.getPermissions(photoId, userId)
            perms.shouldBeEmpty()
            verify { photoRepo.existsById(photoId) }
            verify { userRepo.existsById(userId) }
            verify { authzRepo.fetch(userId, photoId) }
            confirmVerified(photoRepo, userRepo, authzRepo)
        }

        @Test
        fun `should return permissions`() {
            every { photoRepo.existsById(photoId) } returns true
            every { userRepo.existsById(userId) } returns true
            every { authzRepo.fetch(userId, photoId) } returns permissions
            val perms = service.getPermissions(photoId, userId)
            perms shouldBe permissions
            verify { photoRepo.existsById(photoId) }
            verify { userRepo.existsById(userId) }
            verify { authzRepo.fetch(userId, photoId) }
            confirmVerified(photoRepo, userRepo, authzRepo)
        }
    }

    @Nested
    inner class transform {
        private val jpgPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1,
            version = 2
        )
        private val pngPhoto = Photo(
            id = UUID.randomUUID(),
            ownerId = UUID.randomUUID(),
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Png,
            hash = "hash",
            no = 1,
            version = 2
        )
        private val jpgFile = File(javaClass.getResource("/data/photo.jpg").file)
        private val pngFile = File(javaClass.getResource("/data/photo.png").file)

        @BeforeEach
        fun beforeEach() {
            copyPhoto(jpgPhoto, jpgFile)
            copyPhoto(pngPhoto, pngFile)
        }

        @Nested
        inner class rotation {
            private val clockwiseRotationEvent = PhotoEvent.Transform(
                date = OffsetDateTime.now(),
                subjectId = jpgPhoto.id,
                number = 1,
                source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
                transform = Transform.Rotation(90.0)
            )
            private val counterclockwiseRotationEvent = clockwiseRotationEvent.copy(
                transform = Transform.Rotation(-90.0)
            )
            private val jpgClockwiseRotatedFile = File(javaClass.getResource("/data/photo-clockwise.jpg").file)
            private val jpgCounterclockwiseRotatedFile =
                File(javaClass.getResource("/data/photo-counterclockwise.jpg").file)
            private val pngClockwiseRotatedFile = File(javaClass.getResource("/data/photo-clockwise.png").file)
            private val pngCounterclockwiseRotatedFile =
                File(javaClass.getResource("/data/photo-counterclockwise.png").file)

            @Test
            fun `should throw exception if photo does not exist`() {
                every { photoRepo.existsById(jpgPhoto.id) } returns false
                val exception = assertThrows<EntityNotFoundException> {
                    service.transform(jpgPhoto.id, clockwiseRotationEvent.transform, clockwiseRotationEvent.source)
                }
                exception shouldHaveMessage "Photo ${jpgPhoto.id} does not exist"
                verify { photoRepo.existsById(jpgPhoto.id) }
                confirmVerified(photoRepo)
            }

            @Test
            fun `should rotate photo clockwise (jpg)`() {
                every { photoRepo.existsById(jpgPhoto.id) } returns true
                every { photoRepo.fetchById(jpgPhoto.id) } returns jpgPhoto
                every {
                    photoEventRepo.saveTransformEvent(
                        photoId = jpgPhoto.id,
                        transform = clockwiseRotationEvent.transform,
                        source = clockwiseRotationEvent.source
                    )
                } returns clockwiseRotationEvent
                service.transform(jpgPhoto.id, clockwiseRotationEvent.transform, clockwiseRotationEvent.source)
                rawFile(jpgPhoto.id, jpgPhoto.uploadDate, jpgPhoto.type.extension(), 2).readBytes().shouldBe(
                    jpgClockwiseRotatedFile.readBytes()
                )
                verify { photoRepo.existsById(jpgPhoto.id) }
                verify { photoRepo.fetchById(jpgPhoto.id) }
                verify {
                    photoEventRepo.saveTransformEvent(
                        photoId = jpgPhoto.id,
                        transform = clockwiseRotationEvent.transform,
                        source = clockwiseRotationEvent.source
                    )
                }
                confirmVerified(photoRepo, photoEventRepo)
            }

            @Test
            fun `should rotate photo counterclockwise (jpg)`() {
                every { photoRepo.existsById(jpgPhoto.id) } returns true
                every { photoRepo.fetchById(jpgPhoto.id) } returns jpgPhoto
                every {
                    photoEventRepo.saveTransformEvent(
                        photoId = jpgPhoto.id,
                        transform = counterclockwiseRotationEvent.transform,
                        source = counterclockwiseRotationEvent.source
                    )
                } returns counterclockwiseRotationEvent
                service.transform(
                    id = jpgPhoto.id,
                    transform = counterclockwiseRotationEvent.transform,
                    source = counterclockwiseRotationEvent.source
                )
                rawFile(jpgPhoto.id, jpgPhoto.uploadDate, jpgPhoto.type.extension(), 2).readBytes().shouldBe(
                    jpgCounterclockwiseRotatedFile.readBytes()
                )
                verify { photoRepo.existsById(jpgPhoto.id) }
                verify { photoRepo.fetchById(jpgPhoto.id) }
                verify {
                    photoEventRepo.saveTransformEvent(
                        photoId = jpgPhoto.id,
                        transform = counterclockwiseRotationEvent.transform,
                        source = counterclockwiseRotationEvent.source
                    )
                }
                confirmVerified(photoRepo, photoEventRepo)
            }

            @Test
            fun `should rotate photo clockwise (png)`() {
                every { photoRepo.existsById(pngPhoto.id) } returns true
                every { photoRepo.fetchById(pngPhoto.id) } returns pngPhoto
                every {
                    photoEventRepo.saveTransformEvent(
                        photoId = pngPhoto.id,
                        transform = clockwiseRotationEvent.transform,
                        source = clockwiseRotationEvent.source
                    )
                } returns clockwiseRotationEvent
                service.transform(pngPhoto.id, clockwiseRotationEvent.transform, clockwiseRotationEvent.source)
                rawFile(pngPhoto.id, pngPhoto.uploadDate, pngPhoto.type.extension(), 2).readBytes().shouldBe(
                    pngClockwiseRotatedFile.readBytes()
                )
                verify { photoRepo.existsById(pngPhoto.id) }
                verify { photoRepo.fetchById(pngPhoto.id) }
                verify {
                    photoEventRepo.saveTransformEvent(
                        photoId = pngPhoto.id,
                        transform = clockwiseRotationEvent.transform,
                        source = clockwiseRotationEvent.source
                    )
                }
                confirmVerified(photoRepo, photoEventRepo)
            }

            @Test
            fun `should rotate photo counterclockwise (png)`() {
                every { photoRepo.existsById(pngPhoto.id) } returns true
                every { photoRepo.fetchById(pngPhoto.id) } returns pngPhoto
                every {
                    photoEventRepo.saveTransformEvent(
                        photoId = pngPhoto.id,
                        transform = counterclockwiseRotationEvent.transform,
                        source = counterclockwiseRotationEvent.source
                    )
                } returns counterclockwiseRotationEvent
                service.transform(
                    id = pngPhoto.id,
                    transform = counterclockwiseRotationEvent.transform,
                    source = counterclockwiseRotationEvent.source
                )
                rawFile(pngPhoto.id, pngPhoto.uploadDate, pngPhoto.type.extension(), 2).readBytes().shouldBe(
                    pngCounterclockwiseRotatedFile.readBytes()
                )
                verify { photoRepo.existsById(pngPhoto.id) }
                verify { photoRepo.fetchById(pngPhoto.id) }
                verify {
                    photoEventRepo.saveTransformEvent(
                        photoId = pngPhoto.id,
                        transform = counterclockwiseRotationEvent.transform,
                        source = counterclockwiseRotationEvent.source
                    )
                }
                confirmVerified(photoRepo, photoEventRepo)
            }
        }
    }

    @Nested
    inner class update {
        private val event = PhotoEvent.Update(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            content = PhotoEvent.Update.Content()
        )
        private val photo = Photo(
            id = event.subjectId,
            ownerId = event.source.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1,
            version = 1
        )

        @Test
        fun `should throw exception if photo does not exist`() {
            every { photoRepo.existsById(event.subjectId) } returns false
            val exception = assertThrows<EntityNotFoundException> {
                service.update(event.subjectId, event.content.shootingDate, event.source)
            }
            exception shouldHaveMessage "Photo ${event.subjectId} does not exist"
            verify { photoRepo.existsById(event.subjectId) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should update photo`() {
            every { photoRepo.existsById(event.subjectId) } returns true
            every { photoEventRepo.saveUpdateEvent(event.subjectId, event.content, event.source) } returns event
            every { photoRepo.fetchById(event.subjectId) } returns photo
            service.update(event.subjectId, event.content.shootingDate, event.source)
            verify { photoRepo.existsById(event.subjectId) }
            verify { photoEventRepo.saveUpdateEvent(event.subjectId, event.content, event.source) }
            verify { photoRepo.fetchById(event.subjectId) }
            confirmVerified(photoRepo, photoEventRepo)
        }
    }

    @Nested
    inner class updatePermissions {
        private val owner = User(
            id = UUID.randomUUID(),
            name = "owner",
            hashedPwd = "hashedPwd",
            role = Role.User,
            creationDate = OffsetDateTime.now()
        )
        private val user = User(
            id = UUID.randomUUID(),
            name = "user",
            hashedPwd = "hashedPwd",
            role = Role.User,
            creationDate = OffsetDateTime.now()
        )
        private val photo = Photo(
            id = UUID.randomUUID(),
            ownerId = owner.id,
            uploadDate = OffsetDateTime.now(),
            type = Photo.Type.Jpg,
            hash = "hash",
            no = 1,
            version = 1
        )
        private val permissionsToAdd = setOf(
            UserPermissions(
                user = user,
                permissions = setOf(Permission.Read, Permission.Delete)
            )
        )
        private val permissionsToRemove = setOf(
            UserPermissions(
                user = user,
                permissions = setOf(Permission.Delete)
            )
        )
        private val event = PhotoEvent.UpdatePermissions(
            date = OffsetDateTime.now(),
            subjectId = photo.id,
            number = 2,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            content = PhotoEvent.UpdatePermissions.Content(
                permissionsToAdd = setOf(
                    SubjectPermissions(
                        subjectId = user.id,
                        permissions = setOf(Permission.Read, Permission.Delete)
                    )
                ),
                permissionsToRemove = setOf(
                    SubjectPermissions(
                        subjectId = user.id,
                        permissions = setOf(Permission.Delete)
                    )
                )
            )
        )

        @Test
        fun `should throw exception if photo does not exist`() {
            every { photoRepo.fetchById(photo.id) } returns null
            val exception = assertThrows<EntityNotFoundException> {
                service.updatePermissions(photo.id, permissionsToAdd, permissionsToRemove, event.source)
            }
            exception shouldHaveMessage "Photo ${photo.id} does not exist"
            verify { photoRepo.fetchById(photo.id) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should throw exception if owner permission is deleted`() {
            every { photoRepo.fetchById(photo.id) } returns photo
            assertThrows<OwnerPermissionsUpdateException> {
                service.updatePermissions(
                    id = photo.id,
                    permissionsToAdd = permissionsToAdd,
                    permissionsToRemove = setOf(
                        UserPermissions(
                            user = owner,
                            permissions = setOf(Permission.Delete)
                        )
                    ),
                    source = event.source
                )
            }
            verify { photoRepo.fetchById(photo.id) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should update permissions of photo`() {
            every { photoRepo.fetchById(photo.id) } returns photo
            every { photoEventRepo.saveUpdatePermissionsEvent(photo.id, event.content, event.source) } returns event
            service.updatePermissions(photo.id, permissionsToAdd, permissionsToRemove, event.source)
            verify { photoRepo.fetchById(photo.id) }
            verify { photoEventRepo.saveUpdatePermissionsEvent(photo.id, event.content, event.source) }
            confirmVerified(photoRepo, photoEventRepo)
        }
    }

    @Nested
    inner class upload {
        private val jpgFile = File(javaClass.getResource("/data/photo.jpg").file)
        private val pngFile = File(javaClass.getResource("/data/photo.png").file)
        private val jpgEvent = PhotoEvent.Upload(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            content = PhotoEvent.Upload.Content(
                type = Photo.Type.Jpg,
                hash = "fbb07273b91a57319264be345567a6e8b2537abd"
            )
        )
        private val pngEvent = PhotoEvent.Upload(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            number = 1,
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            content = PhotoEvent.Upload.Content(
                type = Photo.Type.Png,
                hash = "06e207dd71ed0874b55fe84cabfa41ec3f1938aa"
            )
        )
        private val jpgPhoto = Photo(
            id = jpgEvent.subjectId,
            ownerId = jpgEvent.source.id,
            uploadDate = jpgEvent.date,
            type = jpgEvent.content.type,
            hash = jpgEvent.content.hash,
            no = 1,
            version = 1
        )
        private val pngPhoto = Photo(
            id = pngEvent.subjectId,
            ownerId = pngEvent.source.id,
            uploadDate = pngEvent.date,
            type = pngEvent.content.type,
            hash = pngEvent.content.hash,
            no = 2,
            version = 1
        )

        @Test
        fun `should throw exception if hash already exists`() {
            every { photoRepo.fetchByHash(jpgEvent.source.id, jpgEvent.content.hash) } returns jpgPhoto
            val exception = assertThrows<PhotoAlreadyUploadedException> {
                service.upload(jpgFile.inputStream(), Photo.Type.Jpg, jpgEvent.source)
            }
            exception.photo shouldBe jpgPhoto
            verify { photoRepo.fetchByHash(jpgEvent.source.id, jpgEvent.content.hash) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return upload event (jpg)`() {
            every { photoRepo.fetchByHash(jpgEvent.source.id, jpgEvent.content.hash) } returns null
            every { photoEventRepo.saveUploadEvent(jpgEvent.content, jpgEvent.source) } returns jpgEvent
            every { photoRepo.fetchById(jpgEvent.subjectId) } returns jpgPhoto
            val photo = service.upload(jpgFile.inputStream(), Photo.Type.Jpg, jpgEvent.source)
            photo shouldBe jpgPhoto
            rawFile(jpgEvent.subjectId, jpgEvent.date, "jpg").readBytes() shouldBe jpgFile.readBytes()
            compressedFile(jpgEvent.subjectId, jpgEvent.date, "jpg").shouldExist()
            verify { photoRepo.fetchByHash(jpgEvent.source.id, jpgEvent.content.hash) }
            verify { photoEventRepo.saveUploadEvent(jpgEvent.content, jpgEvent.source) }
            verify { photoRepo.fetchById(jpgEvent.subjectId) }
            confirmVerified(photoEventRepo, photoRepo)
        }

        @Test
        fun `should return upload event (png)`() {
            every { photoRepo.fetchByHash(pngEvent.source.id, pngEvent.content.hash) } returns null
            every { photoEventRepo.saveUploadEvent(pngEvent.content, pngEvent.source) } returns pngEvent
            every { photoRepo.fetchById(pngEvent.subjectId) } returns pngPhoto
            val photo = service.upload(pngFile.inputStream(), Photo.Type.Png, pngEvent.source)
            photo shouldBe pngPhoto
            rawFile(pngEvent.subjectId, pngEvent.date, "png").readBytes() shouldBe pngFile.readBytes()
            compressedFile(pngEvent.subjectId, pngEvent.date, "png").shouldExist()
            verify { photoEventRepo.saveUploadEvent(pngEvent.content, pngEvent.source) }
            verify { photoRepo.fetchByHash(pngEvent.source.id, pngEvent.content.hash) }
            verify { photoRepo.fetchById(pngEvent.subjectId) }
            confirmVerified(photoEventRepo, photoRepo)
        }
    }

    @Nested
    inner class userCanReadAll {
        private val photoIds = setOf(UUID.randomUUID())
        private val userId = UUID.randomUUID()

        @Test
        fun `should return true if set of ids is empty`() {
            service.userCanReadAll(emptySet(), userId).shouldBeTrue()
        }

        @Test
        fun `should return false if user does not have permission to read all photos`() {
            every { authzRepo.userCanReadAll(photoIds, userId) } returns false
            service.userCanReadAll(photoIds, userId).shouldBeFalse()
            verify { authzRepo.userCanReadAll(photoIds, userId) }
            confirmVerified(authzRepo)
        }

        @Test
        fun `should return true if user have permission to read all photos`() {
            every { authzRepo.userCanReadAll(photoIds, userId) } returns true
            service.userCanReadAll(photoIds, userId).shouldBeTrue()
            verify { authzRepo.userCanReadAll(photoIds, userId) }
            confirmVerified(authzRepo)
        }
    }

    private fun compressedFile(id: UUID, uploadDate: OffsetDateTime, extension: String, version: Int = 1) = photoFile(
        rootDir = Props.dataDir.resolve(PhotoServiceImpl.CompressedDirname),
        id = id,
        uploadDate = uploadDate,
        extension = extension,
        version = version
    )

    private fun copyFile(srcFile: File, destFile: File) {
        destFile.parentFile.mkdirs()
        srcFile.copyTo(destFile)
    }

    private fun copyPhoto(photo: Photo, file: File, version: Int = 1) {
        copyFile(file, rawFile(photo.id, photo.uploadDate, photo.type.extension(), version))
        copyFile(file, compressedFile(photo.id, photo.uploadDate, photo.type.extension(), version))
    }

    private fun photoFile(rootDir: File, id: UUID, uploadDate: OffsetDateTime, extension: String, version: Int) =
        uploadDate.let { date ->
            rootDir
                .resolve(date.year.toString())
                .resolve(date.monthValue.toString())
                .resolve(date.dayOfMonth.toString())
                .resolve("${id}_$version.$extension")
        }

    private fun rawFile(id: UUID, uploadDate: OffsetDateTime, extension: String, version: Int = 1) = photoFile(
        rootDir = Props.dataDir.resolve(PhotoServiceImpl.RawDirname),
        id = id,
        uploadDate = uploadDate,
        extension = extension,
        version = version
    )

    private fun Photo.Type.extension() = when (this) {
        Photo.Type.Jpg -> "jpg"
        Photo.Type.Png -> "png"
    }
}
