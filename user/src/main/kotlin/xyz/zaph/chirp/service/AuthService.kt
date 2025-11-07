package xyz.zaph.chirp.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.zaph.chirp.domain.exception.EmailNotVerifiedException
import xyz.zaph.chirp.domain.exception.InvalidCredentialsException
import xyz.zaph.chirp.domain.exception.InvalidTokenException
import xyz.zaph.chirp.domain.exception.UserAlreadyExistsException
import xyz.zaph.chirp.domain.exception.UserNotFoundException
import xyz.zaph.chirp.domain.model.AuthenticatedUser
import xyz.zaph.chirp.domain.model.User
import xyz.zaph.chirp.domain.model.UserID
import xyz.zaph.chirp.infra.database.entities.RefreshTokenEntity
import xyz.zaph.chirp.infra.database.entities.UserEntity
import xyz.zaph.chirp.infra.database.mappers.toUser
import xyz.zaph.chirp.infra.database.repositories.RefreshTokenRepository
import xyz.zaph.chirp.infra.database.repositories.UserRepository
import xyz.zaph.chirp.infra.security.PasswordEncoder
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService
) {

    @Transactional
    fun register(email: String, username: String, password: String): User {
        val trimmedEmail = email.trim()
        val user = userRepository.findByEmailOrUsername(trimmedEmail, username.trim())
        if (user != null) {
            throw UserAlreadyExistsException()
        }


        val savedUser = userRepository.saveAndFlush(
            UserEntity(
                email = trimmedEmail,
                username = username.trim(),
                hashedPassword = encoder.encode(password)
            )
        )

        val token = emailVerificationService.createVerificationToken(trimmedEmail)
        return savedUser.toUser()

    }

    @Transactional
    fun logout(refreshToken: String) {
        val useriD = jwtService.getUserIdFromToken(refreshToken)
        val hashed = hashToken(refreshToken)

        refreshTokenRepository.deleteByUserIDAndHashedToken(useriD, hashed)
    }

    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim()) ?: throw InvalidCredentialsException()

        if (!encoder.matches(password, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if (!user.hasVerifiedEmail) {
            throw EmailNotVerifiedException()
        }

        return user.id?.let { userID ->
            val accessToken = jwtService.generateAccessToken(userID)
            val refreshToken = jwtService.generateRefreshToken(userID)

            storeRefreshToken(userID, refreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundException()

    }

    @Transactional
    fun refresh(token: String): AuthenticatedUser {
        if(!jwtService.validateRefreshToken(token)) {
            throw InvalidTokenException(
                message = "Invalid refresh token",
            )
        }

        val userid = jwtService.getUserIdFromToken(token)
        val user = userRepository.findByIdOrNull(userid) ?: throw UserNotFoundException()

        val hashed = hashToken(token)


        return user.id?.let { userid ->
            refreshTokenRepository.findByUserIDAndHashedToken(
                userid,
                hashed
            ) ?: throw InvalidTokenException(
                "Invalid refresh token"
            )

            refreshTokenRepository.deleteByUserIDAndHashedToken(
                userid,
                hashed
            )

            val newAccess = jwtService.generateAccessToken(userid)
            val newRefresh = jwtService.generateRefreshToken(userid)

            storeRefreshToken(userid, newRefresh)
            AuthenticatedUser(
                user = user.toUser(),
                accessToken = newAccess,
                refreshToken = newRefresh
            )


        } ?: throw UserNotFoundException()

    }

    private fun storeRefreshToken(userID: UserID, token: String) {
        val hashed = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMS
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userID = userID,
                expiresAt = expiresAt,
                hashedToken = hashed
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}