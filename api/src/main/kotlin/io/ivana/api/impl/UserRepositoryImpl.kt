package io.ivana.api.impl

import io.ivana.core.User
import io.ivana.core.UserRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*

@Repository
class UserRepositoryImpl(
    jdbc: NamedParameterJdbcTemplate
) : UserRepository, AbstractEntityRepository<User>(jdbc) {
    internal companion object {
        const val TableName = "\"user\""
        const val NameColumnName = "name"
        const val PasswordColumnName = "password"
        const val RoleColumnName = "role"
    }

    override val tableName = TableName

    override fun fetchByName(username: String) = fetchBy(NameColumnName, username)

    override fun entityFromResultSet(rs: ResultSet) = User(
        id = rs.getObject(IdColumnName, UUID::class.java),
        name = rs.getString(NameColumnName),
        hashedPwd = rs.getString(PasswordColumnName),
        role = rs.getRole()
    )

    private fun ResultSet.getRole() = getString(RoleColumnName).let { type ->
        RoleData.values()
            .find { it.sqlValue == type }
            ?.role
            ?: throw UnknownPhotoTypeException("Unknown role '$type'")
    }
}
