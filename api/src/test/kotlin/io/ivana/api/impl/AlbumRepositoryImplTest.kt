@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.*
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.net.InetAddress
import java.util.*

@SpringBootTest
internal class AlbumRepositoryImplTest {
    private val pwdEncoder = BCryptPasswordEncoder()
    private val initData = listOf(
        InitDataEntry(
            userCreationContent = UserEvent.Creation.Content(
                name = "admin",
                hashedPwd = pwdEncoder.encode("changeit"),
                role = Role.SuperAdmin
            ),
            albumNames = listOf("album1", "album2", "album3")
        ),
        InitDataEntry(
            userCreationContent = UserEvent.Creation.Content(
                name = "gleroy",
                hashedPwd = pwdEncoder.encode("changeit"),
                role = Role.User
            ),
            albumNames = listOf("album1", "album2", "album3")
        )
    )

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var repo: AlbumRepositoryImpl

    @Autowired
    private lateinit var eventRepo: AlbumEventRepository

    @Autowired
    private lateinit var userEventRepo: UserEventRepository

    private lateinit var createdAlbums: List<Album>

    @BeforeEach
    fun beforeEach() {
        cleanDb(jdbc)
        createdAlbums = initData
            .map { entry ->
                val user = userEventRepo.saveCreationEvent(entry.userCreationContent, EventSource.System).toUser()
                entry.albumNames.map { name ->
                    eventRepo.saveCreationEvent(
                        name = name,
                        source = EventSource.User(user.id, InetAddress.getByName("127.0.0.1"))
                    ).toAlbum()
                }
            }
            .flatten()
    }

    @Nested
    inner class count {
        @Test
        fun `should return count of albums of user`() {
            val count = repo.count()
            count shouldBe createdAlbums.size
        }
    }

    @Nested
    inner class `count with owner id` {
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            ownerId = createdAlbums[0].ownerId
        }

        @Test
        fun `should return count of photos of user`() {
            val count = repo.count(ownerId)
            count shouldBe 3
        }
    }

    @Nested
    inner class existsById {
        private lateinit var album: Album

        @BeforeEach
        fun beforeEach() {
            album = createdAlbums[0]
        }

        @Test
        fun `should return false if album does not exist`() {
            val exists = repo.existsById(UUID.randomUUID())
            exists.shouldBeFalse()
        }

        @Test
        fun `should return true if album exists`() {
            val exists = repo.existsById(album.id)
            exists.shouldBeTrue()
        }
    }

    @Nested
    inner class fetchAll {
        @Test
        fun `should return all albums in interval`() {
            val albums = repo.fetchAll(1, 10)
            albums shouldBe createdAlbums.sortedBy { it.id.toString() }.subList(1, createdAlbums.size)
        }
    }

    @Nested
    inner class `fetchAll with owner id` {
        private lateinit var ownerId: UUID

        @BeforeEach
        fun beforeEach() {
            ownerId = createdAlbums[0].ownerId
        }

        @Test
        fun `should return all photos in interval`() {
            val photos = repo.fetchAll(ownerId, 1, 10)
            photos shouldBe createdAlbums.subList(1, 3)
        }
    }

    @Nested
    inner class fetchAllByIds {
        @Test
        fun `should return empty set if ids is empty`() {
            val albums = repo.fetchAllByIds(emptySet())
            albums.shouldBeEmpty()
        }

        @Test
        fun `should return all albums`() {
            val albums = repo.fetchAllByIds(createdAlbums.map { it.id }.toSet())
            albums shouldContainExactlyInAnyOrder createdAlbums
        }
    }

    @Nested
    inner class fetchById {
        private lateinit var album: Album

        @BeforeEach
        fun beforeEach() {
            album = createdAlbums[0]
        }

        @Test
        fun `should return null if album does not exist`() {
            val album = repo.fetchById(UUID.randomUUID())
            album.shouldBeNull()
        }

        @Test
        fun `should return album with id`() {
            val album = repo.fetchById(album.id)
            album shouldBe album
        }
    }

    @Nested
    inner class fetchExistingIds {
        private lateinit var expectedExistingIds: Set<UUID>

        @BeforeEach
        fun beforeEach() {
            expectedExistingIds = createdAlbums.map { it.id }.toSet()
        }

        @Test
        fun `should return existing ids`() {
            val existingIds = repo.fetchExistingIds(expectedExistingIds + setOf(UUID.randomUUID()))
            existingIds shouldBe expectedExistingIds
        }
    }

    private fun AlbumEvent.Creation.toAlbum() = Album(
        id = subjectId,
        ownerId = source.id,
        name = albumName,
        creationDate = date
    )

    private data class InitDataEntry(
        val userCreationContent: UserEvent.Creation.Content,
        val albumNames: List<String>
    )
}
