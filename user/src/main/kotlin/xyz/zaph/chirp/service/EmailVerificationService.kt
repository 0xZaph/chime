package xyz.zaph.chirp.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.zaph.chirp.domain.exception.InvalidTokenException
import xyz.zaph.chirp.domain.exception.UserNotFoundException
import xyz.zaph.chirp.domain.model.EmailVerificationToken
import xyz.zaph.chirp.infra.database.entities.EmailVerificationTokenEntity
import xyz.zaph.chirp.infra.database.mappers.toEmailVerificationToken
import xyz.zaph.chirp.infra.database.repositories.EmailVerificationTokenRepository
import xyz.zaph.chirp.infra.database.repositories.UserRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chirp.email.verification.expiry-hours}") private val expiryHours: Long
) {

    @Transactional
    fun createVerificationToken(email: String) : EmailVerificationToken {
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        emailVerificationTokenRepository.invalidateActiveTokensForUser(user)

        val newToken = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = user,
        )

        return emailVerificationTokenRepository.save(newToken).toEmailVerificationToken()

    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException(
                "Email verification token invalid"
            )

        if (verificationToken.isUsed) {
            throw InvalidTokenException(
                "Email verification token is already used"
            )
        }

        if (verificationToken.isExpired) {
            throw InvalidTokenException(
                "Email verification token is expired"
            )
        }

        emailVerificationTokenRepository.save(
            verificationToken.apply {
                usedAt = Instant.now()
            }
        )

        userRepository.save(
            verificationToken.user.apply {
                hasVerifiedEmail = true
            }
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanUpExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}