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
    inner class getTimeWindowById {
        private val defaultTimeWindow = PhotosTimeWindow(
            current = Photo(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                uploadDate = OffsetDateTime.now(),
                type = Photo.Type.Jpg,
                hash = "hash",
                no = 2
            )
        )
        private val completeTimeWindow = defaultTimeWindow.copy(
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
            every { photoRepo.fetchById(defaultTimeWindow.current.id) } returns null
            val exception = assertThrows<EntityNotFoundException> {
                service.getTimeWindowById(defaultTimeWindow.current.id)
            }
            exception shouldHaveMessage "Photo ${defaultTimeWindow.current.id} does not exist"
            verify { photoRepo.fetchById(defaultTimeWindow.current.id) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return default time window`() {
            every { photoRepo.fetchById(defaultTimeWindow.current.id) } returns defaultTimeWindow.current
            every { photoRepo.fetchPreviousOf(defaultTimeWindow.current) } returns null
            every { photoRepo.fetchNextOf(defaultTimeWindow.current) } returns null
            val timeWindow = service.getTimeWindowById(defaultTimeWindow.current.id)
            timeWindow shouldBe defaultTimeWindow
            verify { photoRepo.fetchById(defaultTimeWindow.current.id) }
            verify { photoRepo.fetchPreviousOf(defaultTimeWindow.current) }
            verify { photoRepo.fetchNextOf(defaultTimeWindow.current) }
            confirmVerified(photoRepo)
        }

        @Test
        fun `should return complete time window`() {
            every { photoRepo.fetchById(completeTimeWindow.current.id) } returns completeTimeWindow.current
            every { photoRepo.fetchPreviousOf(completeTimeWindow.current) } returns completeTimeWindow.previous
            every { photoRepo.fetchNextOf(completeTimeWindow.current) } returns completeTimeWindow.next
            val timeWindow = service.getTimeWindowById(completeTimeWindow.current.id)
            timeWindow shouldBe completeTimeWindow
            verify { photoRepo.fetchById(completeTimeWindow.current.id) }
            verify { photoRepo.fetchPreviousOf(completeTimeWindow.current) }
            verify { photoRepo.fetchNextOf(defaultTimeWindow.current) }
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
}
