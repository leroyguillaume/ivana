package io.ivana.api.impl

import io.ivana.api.config.IvanaProperties
import io.ivana.core.*
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.codec.Hex
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.*
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.math.*


@Service
class PhotoServiceImpl(
    override val repo: PhotoRepository,
    private val eventRepo: PhotoEventRepository,
    private val props: IvanaProperties
) : PhotoService, AbstractEntityService<Photo>() {
    internal companion object {
        const val RawDirname = "raw"
        const val CompressedDirname = "compressed"

        private val Digest = MessageDigest.getInstance("SHA1")

        private val Logger = LoggerFactory.getLogger(PhotoServiceImpl::class.java)
    }

    override val entityName = "Photo"

    @Transactional
    override fun delete(id: UUID, source: EventSource.User) {
        if (!repo.existsById(id)) {
            throw EntityNotFoundException("$entityName $id does not exist")
        }
        eventRepo.saveDeletionEvent(id, source)
        Logger.info("User ${source.id} (${source.ip}) deleted photo $id")
    }

    override fun getAll(ownerId: UUID, pageNo: Int, pageSize: Int): Page<Photo> {
        val content = repo.fetchAll(ownerId, (pageNo - 1) * pageSize, pageSize)
        val itemsNb = repo.count(ownerId)
        return Page(
            content = content,
            no = pageNo,
            totalItems = itemsNb,
            totalPages = ceil(itemsNb.toDouble() / pageSize.toDouble()).toInt()
        )
    }

    override fun getCompressedFile(photo: Photo) = compressedFile(photo.id, photo.uploadDate, photo.type)

    override fun getRawFile(photo: Photo) = rawFile(photo.id, photo.uploadDate, photo.type)

    override fun getLinkedById(id: UUID) = getById(id).let { photo ->
        LinkedPhotos(
            current = photo,
            previous = repo.fetchPreviousOf(photo),
            next = repo.fetchNextOf(photo)
        )
    }

    @Transactional
    override fun transform(id: UUID, transform: Transform, source: EventSource.User) {
        transform.perform(getById(id))
        eventRepo.saveTransformEvent(id, transform, source)
        Logger.info("User ${source.id} (${source.ip}) transformed photo $id")
    }

    @Transactional
    override fun uploadPhoto(input: InputStream, type: Photo.Type, source: EventSource.User): Photo {
        val tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".${type.extension()}")
        tmpFile.deleteOnExit()
        try {
            tmpFile.outputStream().use { input.copyTo(it) }
            val hash = tmpFile.hash()
            val photo = repo.fetchByHash(source.id, hash)
            if (photo != null) {
                throw PhotoAlreadyUploadedException(photo)
            }
            val content = PhotoEvent.Upload.Content(
                type = type,
                hash = hash
            )
            val event = eventRepo.saveUploadEvent(content, source)
            val rawFile = saveRawFile(tmpFile, event)
            try {
                val compressionFile = saveCompressedFile(tmpFile, event)
                return try {
                    Logger.info("User ${source.id} (${source.ip}) uploaded new photo (${event.subjectId})")
                    repo.fetchById(event.subjectId)!!
                } catch (exception: Exception) {
                    compressionFile.deleteAndLog()
                    throw exception
                }
            } catch (exception: Exception) {
                rawFile.deleteAndLog()
                throw exception
            }
        } finally {
            tmpFile.deleteAndLog()
        }
    }

    private fun compressedFile(id: UUID, uploadDate: OffsetDateTime, type: Photo.Type) = photoFile(
        rootDir = props.dataDir.resolve(CompressedDirname),
        id = id,
        uploadDate = uploadDate,
        type = type
    )

    private fun performRotation(photo: Photo, direction: Transform.Rotation.Direction) {
        performRotation(rawFile(photo.id, photo.uploadDate, photo.type), photo.type, direction)
        performRotation(compressedFile(photo.id, photo.uploadDate, photo.type), photo.type, direction)
    }

    private fun performRotation(file: File, type: Photo.Type, direction: Transform.Rotation.Direction) {
        val image = file.inputStream().use { ImageIO.read(it) }
        val angle = Math.toRadians(direction.angle)
        val sin = abs(sin(angle))
        val cos = abs(cos(angle))
        val width = floor(image.width * cos + image.height * sin).toInt()
        val height = floor(image.height * cos + image.width * sin).toInt()
        val rotatedImage = BufferedImage(width, height, image.type)
        val transform = AffineTransform()
        transform.translate(width / 2.toDouble(), height / 2.toDouble())
        transform.rotate(angle, 0.0, 0.0)
        transform.translate(-image.width / 2.toDouble(), -image.height / 2.toDouble())
        val transformOp = AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR)
        transformOp.filter(image, rotatedImage)
        file.outputStream().use { ImageIO.write(rotatedImage, type.format(), it) }
    }

    private fun photoFile(rootDir: File, id: UUID, uploadDate: OffsetDateTime, type: Photo.Type) =
        uploadDate.let { date ->
            rootDir
                .resolve(date.year.toString())
                .resolve(date.monthValue.toString())
                .resolve(date.dayOfMonth.toString())
                .resolve("$id.${type.extension()}")
        }

    private fun rawFile(id: UUID, uploadDate: OffsetDateTime, type: Photo.Type) = photoFile(
        rootDir = props.dataDir.resolve(RawDirname),
        id = id,
        uploadDate = uploadDate,
        type = type
    )

    private fun saveCompressedFile(tmpFile: File, event: PhotoEvent.Upload) =
        compressedFile(event.subjectId, event.date, event.content.type).apply {
            parentFile.mkdirs()
            val format = event.content.type.format()
            val writers = ImageIO.getImageWritersByFormatName(format)
            if (!writers.hasNext()) {
                throw UnsupportedImageFormatException("No image writer for format '$format'")
            }
            val writer = writers.next()
            val writeParam = writer.defaultWriteParam.apply {
                compressionMode = ImageWriteParam.MODE_EXPLICIT
                compressionQuality = props.compressionQuality
            }
            outputStream().use { out ->
                writer.output = ImageIO.createImageOutputStream(out)
                writer.write(null, IIOImage(ImageIO.read(tmpFile.inputStream()), null, null), writeParam)
            }
            Logger.info("Compressed photo ${event.subjectId} saved to $absolutePath")
        }

    private fun saveRawFile(tmpFile: File, event: PhotoEvent.Upload) =
        rawFile(event.subjectId, event.date, event.content.type).apply {
            parentFile.mkdirs()
            tmpFile.copyTo(this)
            Logger.info("Photo ${event.subjectId} uploaded to $absolutePath")
        }

    private fun File.deleteAndLog() {
        if (delete()) {
            Logger.info("File $absolutePath deleted")
        } else {
            Logger.warn("Unable to delete file $absolutePath")
        }
    }

    @Suppress("ControlFlowWithEmptyBody")
    private fun File.hash() = DigestInputStream(inputStream(), Digest).use { input ->
        val buffer = ByteArray(4096)
        while (input.read(buffer) > -1) {
        }
        String(Hex.encode(input.messageDigest.digest())).toLowerCase()
    }

    private fun Photo.Type.extension() = when (this) {
        Photo.Type.Jpg -> "jpg"
        Photo.Type.Png -> "png"
    }

    private fun Photo.Type.format() = when (this) {
        Photo.Type.Jpg -> "jpg"
        Photo.Type.Png -> "png"
    }

    private fun Transform.perform(photo: Photo) = when (this) {
        is Transform.Rotation -> performRotation(photo, direction)
    }
}
