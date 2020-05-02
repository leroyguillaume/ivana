package io.ivana.api.impl

import io.ivana.core.User
import io.ivana.core.UserRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.*

@Repository
class UserRepositoryImpl(
    override val jdbc: NamedParameterJdbcTemplate
) : UserRepository, AbstractEntityRepository<User>() {
    internal companion object {
        const val TableName = "\"user\""
        const val NameColumnName = "name"
        const val PasswordColumnName = "password"
        const val RoleColumnName = "role"
        const val CreationDateColumnName = "creation_date"
    }

    override val tableName = TableName

    override fun fetchByName(username: String) = fetchBy(NameColumnName, username)

    override fun ResultSet.toEntity() = User(
        id = getObject(IdColumnName, UUID::class.java),
        name = getString(NameColumnName),
        hashedPwd = getString(PasswordColumnName),
        role = getRole(),
        creationDate = getObject(CreationDateColumnName, OffsetDateTime::class.java)
    )

    private fun ResultSet.getRole() = getString(RoleColumnName).let { type ->
        RoleData.values()
            .find { it.sqlValue == type }
            ?.role
            ?: throw UnknownPhotoTypeException("Unknown role '$type'")
    }
}
