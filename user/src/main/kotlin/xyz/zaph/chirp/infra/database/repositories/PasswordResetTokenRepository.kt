package xyz.zaph.chirp.infra.database.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import xyz.zaph.chirp.infra.database.entities.EmailVerificationTokenEntity
import xyz.zaph.chirp.infra.database.entities.PasswordResetTokenEntity
import xyz.zaph.chirp.infra.database.entities.UserEntity
import java.time.Instant

interface PasswordResetTokenRepository: JpaRepository<PasswordResetTokenEntity, Long> {
    fun findByToken(token: String): PasswordResetTokenEntity?
    fun deleteByExpiresAtLessThan(now: Instant)

    @Modifying
    @Query("""
        UPDATE PasswordResetTokenEntity p
        set p.usedAt = current timestamp 
        where p.user = :user
    """)
    fun invalidateActiveTokensForUser(user: UserEntity)
}