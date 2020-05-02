package io.ivana.api.impl

import io.ivana.core.UserAlbumAuthorizationRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserAlbumAuthorizationRepositoryImpl(
    jdbc: NamedParameterJdbcTemplate
) : UserAlbumAuthorizationRepository, AbstractAuthorizationRepository(jdbc) {
    internal companion object {
        const val TableName = "user_album_authorization"

        const val UserIdColumnName = "user_id"
        const val AlbumIdColumnName = "album_id"
    }

    override val tableName = TableName
    override val subjectIdColumnName = UserIdColumnName
    override val resourceIdColumnName = AlbumIdColumnName
}
