package io.ivana.api.security

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserAlbumAuthorizationRepository(
    jdbc: NamedParameterJdbcTemplate
) : AbstractAuthorizationRepository(jdbc) {
    internal companion object {
        const val TableName = "user_album_authorization"

        const val UserIdColumnName = "user_id"
        const val AlbumIdColumnName = "album_id"
    }

    override val tableName = TableName
    override val subjectIdColumnName = UserIdColumnName
    override val resourceIdColumnName = AlbumIdColumnName
}
