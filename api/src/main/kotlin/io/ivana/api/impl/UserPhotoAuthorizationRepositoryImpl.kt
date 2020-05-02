package io.ivana.api.impl

import io.ivana.core.UserPhotoAuthorizationRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserPhotoAuthorizationRepositoryImpl(
    jdbc: NamedParameterJdbcTemplate
) : UserPhotoAuthorizationRepository, AbstractAuthorizationRepository(jdbc) {
    internal companion object {
        const val TableName = "user_photo_authorization"

        const val UserIdColumnName = "user_id"
        const val PhotoIdColumnName = "photo_id"
    }

    override val tableName = TableName
    override val subjectIdColumnName = UserIdColumnName
    override val resourceIdColumnName = PhotoIdColumnName
}
