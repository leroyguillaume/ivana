package io.ivana.api.impl

import io.ivana.api.config.IvanaProperties
import io.ivana.core.*
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.codec.Hex
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.*
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

@Service
class PhotoServiceImpl(
    private val photoRepo: PhotoRepository,
    private val photoEventRepo: PhotoEventRepository,
    private val props: IvanaProperties
) : PhotoService {
    internal companion object {
        const val RawDirname = "raw"
        const val CompressedDirname = "compressed"

        private val Digest = MessageDigest.getInstance("SHA1")

        private val Logger = LoggerFactory.getLogger(PhotoServiceImpl::class.java)
    }

    override fun getById(id: UUID) = photoRepo.fetchById(id)
        ?: throw EntityNotFoundException("Photo $id does not exist")

    override fun getTimeWindowById(id: UUID) = getById(id).let { photo ->
        PhotosTimeWindow(
            current = photo,
            previous = photoRepo.fetchPreviousOf(photo),
            next = photoRepo.fetchNextOf(photo)
        )
    }

    override fun uploadPhoto(input: InputStream, type: Photo.Type, source: EventSource.User): Photo {
        val tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".${type.extension()}")
        tmpFile.deleteOnExit()
        try {
            tmpFile.outputStream().use { input.copyTo(it) }
            val hash = tmpFile.hash()
            val photo = photoRepo.fetchByHash(source.id, hash)
            if (photo != null) {
                throw PhotoAlreadyUploadedException(photo)
            }
            val content = PhotoEvent.Upload.Content(
                type = type,
                hash = hash
            )
            val event = photoEventRepo.saveUploadEvent(content, source)
            val rawFile = saveRawFile(tmpFile, event)
            try {
                val compressionFile = saveCompressedFile(tmpFile, event)
                return try {
                    photoRepo.fetchById(event.subjectId)!!
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
}
