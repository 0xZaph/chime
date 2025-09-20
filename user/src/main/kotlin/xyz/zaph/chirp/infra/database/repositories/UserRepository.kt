package xyz.zaph.chirp.infra.database.repositories

import org.springframework.data.jpa.repository.JpaRepository
import xyz.zaph.chirp.domain.model.UserID
import xyz.zaph.chirp.infra.database.entities.UserEntity

interface UserRepository: JpaRepository<UserEntity, UserID> {
    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(
        email: String,
        username: String
    ): UserEntity?

}