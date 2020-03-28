package io.ivana.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ivana.api.config.AuthenticationProperties
import io.ivana.core.EventSource
import io.ivana.core.UserEventRepository
import io.ivana.core.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.time.Clock
import java.util.*

@Service
class AuthenticationService(
    private val userEventRepo: UserEventRepository,
    private val userRepo: UserRepository,
    private val props: AuthenticationProperties,
    private val clock: Clock
) : UserDetailsService {
    private companion object {
        val Logger = LoggerFactory.getLogger(AuthenticationService::class.java)
    }

    fun authenticate(username: String, pwd: String, ip: InetAddress): Jwt {
        val user = userRepo.fetchByName(username) ?: throw BadCredentialsException("User '$username' does not exist")
        if (!BCrypt.checkpw(pwd, user.hashedPwd)) {
            throw BadCredentialsException("Wrong password for user '$username'")
        }
        userEventRepo.saveLoginEvent(EventSource.User(user.id, ip))
        Logger.info("User '$username' authenticated")
        val jwt = JWT.create()
            .withSubject(username)
            .withExpiresAt(Date.from(clock.instant().plusSeconds(props.expirationInSeconds.toLong())))
            .sign(Algorithm.HMAC512(props.secret))
        return Jwt(jwt, props.expirationInSeconds)
    }

    override fun loadUserByUsername(username: String) = userRepo.fetchByName(username)?.let { UserPrincipal(it) }
        ?: throw UsernameNotFoundException("User '$username' does not exist")

    fun usernameFromJwt(jwt: String) = try {
        JWT.require(Algorithm.HMAC512(props.secret))
            .build()
            .verify(jwt)
            .subject
    } catch (exception: Exception) {
        throw BadJwtException("Unable to parse '$jwt' as JWT", exception)
    }
}
