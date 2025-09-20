package xyz.zaph.chirp.service

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import xyz.zaph.chirp.domain.exception.InvalidCredentialsException
import xyz.zaph.chirp.domain.exception.InvalidTokenException
import xyz.zaph.chirp.domain.exception.SamePasswordException
import xyz.zaph.chirp.domain.exception.UserNotFoundException
import xyz.zaph.chirp.domain.model.UserID
import xyz.zaph.chirp.infra.database.entities.PasswordResetTokenEntity
import xyz.zaph.chirp.infra.database.repositories.PasswordResetTokenRepository
import xyz.zaph.chirp.infra.database.repositories.RefreshTokenRepository
import xyz.zaph.chirp.infra.database.repositories.UserRepository
import xyz.zaph.chirp.infra.security.PasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder,
    @param:Value("\${chirp.email.password-reset.expiry-minutes}") private val expiryMinutes: Long,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email)
            ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
        )
        passwordResetTokenRepository.save(token)

        // TODO: Inform notification service about password reset trigger to send email

    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Invalid password reset token")

        if (resetToken.isUsed) {
            throw InvalidTokenException(
                "Password reset token is already used"
            )
        }

        if (resetToken.isExpired) {
            throw InvalidTokenException(
                "Password reset token is expired"
            )
        }

        val user = resetToken.user

        if (encoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }

        val hashed = encoder.encode(newPassword)

        userRepository.save(
            user.apply {
                hashedPassword = hashed
            }
        )

        passwordResetTokenRepository.save(
            resetToken.apply {
                usedAt = Instant.now()
            }
        )

        refreshTokenRepository.deleteByUserID(user.id!!)
    }

    @Transactional
    fun changePassword(
        userID: UserID,
        oldPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findByIdOrNull(userID)
            ?: throw UserNotFoundException()

        if (!encoder.matches(oldPassword, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserID(user.id!!)

        val newHashedPassword = encoder.encode(newPassword)
        userRepository.save(
            user.apply {
                hashedPassword = newHashedPassword
            }
        )


    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanUpExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }

}