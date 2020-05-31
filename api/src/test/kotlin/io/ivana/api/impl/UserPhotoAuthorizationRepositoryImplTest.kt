@file:Suppress("ClassName")

package io.ivana.api.impl

import io.ivana.core.Permission
import io.ivana.core.SubjectPermissions
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
internal class UserPhotoAuthorizationRepositoryImplTest : AbstractRepositoryTest() {
    @Nested
    inner class count {
        @Test
        fun `should return count of permissions about photo`() {
            userPhotoAuthzRepo.count(photoUploadEvents[0].subjectId) shouldBe 2
        }
    }

    @Nested
    inner class fetch {
        @Test
        fun `should return null if user does not exist`() {
            val permissions = userPhotoAuthzRepo.fetch(UUID.randomUUID(), photoUploadEvents[0].subjectId)
            permissions.shouldBeNull()
        }

        @Test
        fun `should return null if photo does not exist`() {
            val permissions = userPhotoAuthzRepo.fetch(photoUploadEvents[0].source.id, UUID.randomUUID())
            permissions.shouldBeNull()
        }

        @Test
        fun `should return null if no authorization defined`() {
            val permissions = userPhotoAuthzRepo.fetch(userCreationEvents[2].subjectId, photoUploadEvents[0].subjectId)
            permissions.shouldBeNull()
        }

        @Test
        fun `should return empty set if user does not have rights`() {
            val permissions = userPhotoAuthzRepo.fetch(userCreationEvents[1].subjectId, photoUploadEvents[1].subjectId)
            permissions!!.shouldBeEmpty()
        }

        @Test
        fun `should return all if user has all permissions`() {
            val permissions = userPhotoAuthzRepo.fetch(photoUploadEvents[1].source.id, photoUploadEvents[1].subjectId)
            permissions shouldContainExactly permissions!!.toSet()
        }
    }

    @Nested
    inner class fetchAll {
        private lateinit var expectedSubjsPerms: List<SubjectPermissions>

        @BeforeEach
        fun beforeEach() {
            expectedSubjsPerms = listOf(
                SubjectPermissions(
                    subjectId = userCreationEvents[0].subjectId,
                    permissions = EnumSet.allOf(Permission::class.java)
                ),
                SubjectPermissions(
                    subjectId = userCreationEvents[1].subjectId,
                    permissions = emptySet()
                )
            ).sortedBy { it.subjectId.toString() }
        }

        @Test
        fun `should return all permissions about photos`() {
            val subjsPerms = userPhotoAuthzRepo.fetchAll(photoUploadEvents[1].subjectId, 1, 10)
            subjsPerms shouldBe expectedSubjsPerms.subList(1, expectedSubjsPerms.size)
        }
    }

    @Nested
    inner class photoIsInReadableAlbum {
        @Test
        fun `should return false if photo is not in any album`() {
            val readable = userPhotoAuthzRepo.photoIsInReadableAlbum(
                photoId = photoUploadEvents[6].subjectId,
                userId = userCreationEvents[0].subjectId
            )
            readable.shouldBeFalse()
        }

        @Test
        fun `should return false if photo is in not readable album`() {
            val readable = userPhotoAuthzRepo.photoIsInReadableAlbum(
                photoId = photoUploadEvents[0].subjectId,
                userId = userCreationEvents[2].subjectId
            )
            readable.shouldBeFalse()
        }

        @Test
        fun `should return false if photo is in album but album read is not allowed`() {
            val readable = userPhotoAuthzRepo.photoIsInReadableAlbum(
                photoId = photoUploadEvents[0].subjectId,
                userId = userCreationEvents[2].subjectId
            )
            readable.shouldBeFalse()
        }

        @Test
        fun `should return true if photo is readable`() {
            val readable = userPhotoAuthzRepo.photoIsInReadableAlbum(
                photoId = photoUploadEvents[0].subjectId,
                userId = userCreationEvents[1].subjectId
            )
            readable.shouldBeTrue()
        }
    }

    @Nested
    inner class userCanReadAll {
        @Test
        fun `should return false if all photos don't exist`() {
            userPhotoAuthzRepo.userCanReadAll(setOf(UUID.randomUUID()), userCreationEvents[1].subjectId).shouldBeFalse()
        }

        @Test
        fun `should return false if user can't read all photos (perms not specify)`() {
            val photosIds = setOf(photoUploadEvents[0].subjectId, photoUploadEvents[8].subjectId)
            userPhotoAuthzRepo.userCanReadAll(photosIds, userCreationEvents[1].subjectId).shouldBeFalse()
        }

        @Test
        fun `should return false if user can't read all photos (read not allowed)`() {
            val photosIds = setOf(photoUploadEvents[0].subjectId, photoUploadEvents[1].subjectId)
            userPhotoAuthzRepo.userCanReadAll(photosIds, userCreationEvents[1].subjectId).shouldBeFalse()
        }

        @Test
        fun `should return true if user can read all photos`() {
            val photosIds = setOf(photoUploadEvents[0].subjectId, photoUploadEvents[2].subjectId)
            userPhotoAuthzRepo.userCanReadAll(photosIds, userCreationEvents[1].subjectId).shouldBeTrue()
        }
    }
}
