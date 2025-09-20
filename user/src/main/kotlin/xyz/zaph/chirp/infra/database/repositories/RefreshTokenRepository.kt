package xyz.zaph.chirp.infra.database.repositories

import org.springframework.data.jpa.repository.JpaRepository
import xyz.zaph.chirp.domain.model.UserID
import xyz.zaph.chirp.infra.database.entities.RefreshTokenEntity

interface RefreshTokenRepository: JpaRepository<RefreshTokenEntity, Long> {
    fun findByUserIDAndHashedToken(userId: UserID, hashedToken: String): RefreshTokenEntity?
    fun deleteByUserIDAndHashedToken(userID: UserID, hashedToken: String)
    fun deleteByUserID(userId: UserID)


}