package xyz.zaph.chirp.infra.database.mappers

import xyz.zaph.chirp.domain.model.User
import xyz.zaph.chirp.infra.database.entities.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id = id!!,
        email = email,
        username = username,
        hasEmailVerfied = hasVerifiedEmail
    )
}