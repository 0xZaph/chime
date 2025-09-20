package xyz.zaph.chirp.infra.database.mappers

import xyz.zaph.chirp.domain.model.EmailVerificationToken
import xyz.zaph.chirp.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken() = EmailVerificationToken(
    id = id,
    token = token,
    user = user.toUser()
)