@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.api.config.IvanaProperties
import io.ivana.core.*
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.*
import java.io.File
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*

internal class PhotoServiceImplTest {
    private companion object {
        private val Props = IvanaProperties(
            dataDir = File("./data"),
            compressionQuality = 0.3F
        )

        @AfterAll
        @JvmStatic
        fun afterAll() {
            Props.dataDir.deleteRecursively()
        }
    }

    private lateinit var photoEventRepo: PhotoEventRepository
    private lateinit var photoRepo: PhotoRepository
    private lateinit var service: PhotoServiceImpl

    @BeforeEach
    fun beforeEach() {
        photoEventRepo = mockk()
        photoRepo = mockk()

        service = PhotoServiceImpl(photoRepo, photoEventRepo, Props)
    }

    @Nested
    inner class getAll {
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
                    no = 1
                ),
                Photo(
                    id = UUID.randomUUID(),
                    ownerId = ownerId,
                    uploadDate = OffsetDateTime.now(),
                    type = Photo.Type.Jpg,
                    hash = "hash2",
                    no = 2
                )
            ),
            no = pageNo,
            totalItems = 2,
            totalPages = 1
        )

        @Test
        fun `should return page`() {
            every { photoRepo.fetchAll(ownerId, pageNo - 1, pageSize) } returns expectedPage.content
            every { photoRepo.count(ownerId) } returns 2
            val page = service.getAll(ownerId, pageNo, pageSize)
            page shouldBe expectedPage
            verify { photoRepo.fetchAll(ownerId, pageNo - 1, pageSize) }
            verify { photoRepo.count(ownerId) }
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
            no = 1
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
            no = 1
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
            no = 1
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
                no = 2
            )
        )
        private val completeLinkedPhotos = defaultLinkedPhotos.copy(
            next = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 1
            ),
            previous = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 3
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
        fun `should return default time window`() {
            every { photoRepo.fetchById(defaultLinkedPhotos.current.id) } returns defaultLinkedPhotos.current
            every { photoRepo.fetchPreviousOf(defaultLinkedPhotos.current) } returns null
            every { photoRepo.fetchNextOf(defaultLinkedPhotos.current) } returns null
            val linkedPhotos = service.getLinkedById(defaultLinkedPhotos.current.id)
            linkedPhotos shouldBe defaultLinkedPhotos
            verify { photoRepo.fetchById(defaultLinkedPhotos.current.id) }
            verify { photoRepo.fetchPreviousOf(defaultLinkedPhotos.current) }
            verify { photoRepo.fetchNextOf(defaultLinkedPhotos.current) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return complete time window`() {
            every { photoRepo.fetchById(completeLinkedPhotos.current.id) } returns completeLinkedPhotos.current
            every { photoRepo.fetchPreviousOf(completeLinkedPhotos.current) } returns completeLinkedPhotos.previous
            every { photoRepo.fetchNextOf(completeLinkedPhotos.current) } returns completeLinkedPhotos.next
            val linkedPhotos = service.getLinkedById(completeLinkedPhotos.current.id)
            linkedPhotos shouldBe completeLinkedPhotos
            verify { photoRepo.fetchById(completeLinkedPhotos.current.id) }
            verify { photoRepo.fetchPreviousOf(completeLinkedPhotos.current) }
            verify { photoRepo.fetchNextOf(defaultLinkedPhotos.current) }
            confirmVerified(photoRepo)
        }
    }

    @Nested
    inner class uploadPhoto {
        private val jpgFile = File(javaClass.getResource("/data/photo.jpg").file)
        private val pngFile = File(javaClass.getResource("/data/photo.png").file)
        private val jpgEvent = PhotoEvent.Upload(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
            source = EventSource.User(UUID.randomUUID(), InetAddress.getByName("127.0.0.1")),
            content = PhotoEvent.Upload.Content(
                type = Photo.Type.Jpg,
                hash = "fbb07273b91a57319264be345567a6e8b2537abd"
            )
        )
        private val pngEvent = PhotoEvent.Upload(
            date = OffsetDateTime.now(),
            subjectId = UUID.randomUUID(),
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
            no = 1
        )
        private val pngPhoto = Photo(
            id = pngEvent.subjectId,
            ownerId = pngEvent.source.id,
            uploadDate = pngEvent.date,
            type = pngEvent.content.type,
            hash = pngEvent.content.hash,
            no = 2
        )

        @Test
        fun `should throw exception if hash already exists`() {
            every { photoRepo.fetchByHash(jpgEvent.source.id, jpgEvent.content.hash) } returns jpgPhoto
            val exception = assertThrows<PhotoAlreadyUploadedException> {
                service.uploadPhoto(jpgFile.inputStream(), Photo.Type.Jpg, jpgEvent.source)
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
            val photo = service.uploadPhoto(jpgFile.inputStream(), Photo.Type.Jpg, jpgEvent.source)
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
            val photo = service.uploadPhoto(pngFile.inputStream(), Photo.Type.Png, pngEvent.source)
            photo shouldBe pngPhoto
            rawFile(pngEvent.subjectId, pngEvent.date, "png").readBytes() shouldBe pngFile.readBytes()
            compressedFile(pngEvent.subjectId, pngEvent.date, "png").shouldExist()
            verify { photoEventRepo.saveUploadEvent(pngEvent.content, pngEvent.source) }
            verify { photoRepo.fetchByHash(pngEvent.source.id, pngEvent.content.hash) }
            verify { photoRepo.fetchById(pngEvent.subjectId) }
            confirmVerified(photoEventRepo, photoRepo)
        }
    }

    private fun compressedFile(id: UUID, uploadDate: OffsetDateTime, extension: String) = photoFile(
        rootDir = Props.dataDir.resolve(PhotoServiceImpl.CompressedDirname),
        id = id,
        uploadDate = uploadDate,
        extension = extension
    )

    private fun photoFile(rootDir: File, id: UUID, uploadDate: OffsetDateTime, extension: String) =
        uploadDate.let { date ->
            rootDir
                .resolve(date.year.toString())
                .resolve(date.monthValue.toString())
                .resolve(date.dayOfMonth.toString())
                .resolve("$id.$extension")
        }

    private fun rawFile(id: UUID, uploadDate: OffsetDateTime, extension: String) = photoFile(
        rootDir = Props.dataDir.resolve(PhotoServiceImpl.RawDirname),
        id = id,
        uploadDate = uploadDate,
        extension = extension
    )
}
