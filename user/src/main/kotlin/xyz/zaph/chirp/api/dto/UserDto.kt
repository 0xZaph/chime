package xyz.zaph.chirp.api.dto

import xyz.zaph.chirp.domain.model.UserID

data class UserDto(
    val id: UserID,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean
)
