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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin


@Service
class PhotoServiceImpl(
    override val repo: PhotoRepository,
    private val eventRepo: PhotoEventRepository,
    private val props: IvanaProperties
) : PhotoService, AbstractOwnableEntityService<Photo>() {
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

    override fun getCompressedFile(photo: Photo) = compressedFile(photo.id, photo.uploadDate, photo.type, photo.version)

    override fun getRawFile(photo: Photo) = rawFile(photo.id, photo.uploadDate, photo.type, photo.version)

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

    private fun compressedFile(id: UUID, uploadDate: OffsetDateTime, type: Photo.Type, version: Int = 1) = photoFile(
        rootDir = props.dataDir.resolve(CompressedDirname),
        id = id,
        uploadDate = uploadDate,
        type = type,
        version = version
    )

    private fun performRotation(photo: Photo, direction: Transform.Rotation.Direction) {
        performRotation(
            srcFile = rawFile(photo.id, photo.uploadDate, photo.type, photo.version),
            targetFile = rawFile(photo.id, photo.uploadDate, photo.type, photo.version + 1),
            type = photo.type,
            direction = direction
        )
        performRotation(
            srcFile = compressedFile(photo.id, photo.uploadDate, photo.type, photo.version),
            targetFile = compressedFile(photo.id, photo.uploadDate, photo.type, photo.version + 1),
            type = photo.type,
            direction = direction
        )
    }

    private fun performRotation(
        srcFile: File,
        targetFile: File,
        type: Photo.Type,
        direction: Transform.Rotation.Direction
    ) = try {
        val image = srcFile.inputStream().use { ImageIO.read(it) }
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
        targetFile.outputStream().use { ImageIO.write(rotatedImage, type.format(), it) }
    } catch (exception: Exception) {
        throw TransformException("Unable to perform rotation on photo ${srcFile.absolutePath}", exception)
    }

    private fun photoFile(rootDir: File, id: UUID, uploadDate: OffsetDateTime, type: Photo.Type, version: Int) =
        uploadDate.let { date ->
            rootDir
                .resolve(date.year.toString())
                .resolve(date.monthValue.toString())
                .resolve(date.dayOfMonth.toString())
                .resolve("${id}_$version.${type.extension()}")
        }

    private fun rawFile(id: UUID, uploadDate: OffsetDateTime, type: Photo.Type, version: Int = 1) = photoFile(
        rootDir = props.dataDir.resolve(RawDirname),
        id = id,
        uploadDate = uploadDate,
        type = type,
        version = version
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
