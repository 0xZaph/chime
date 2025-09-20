package xyz.zaph.chirp.infra.database.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import xyz.zaph.chirp.infra.database.entities.EmailVerificationTokenEntity
import xyz.zaph.chirp.infra.database.entities.UserEntity
import java.time.Instant

interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationTokenEntity, Long> {
    fun findByToken(token: String): EmailVerificationTokenEntity?
    fun deleteByExpiresAtLessThan(now: Instant)

    @Modifying
    @Query("""
        UPDATE EmailVerificationTokenEntity e 
        set e.usedAt = current timestamp 
        where  e.user = :user
    """)
    fun invalidateActiveTokensForUser(user: UserEntity)
}