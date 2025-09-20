package xyz.zaph.chirp.domain.model

import java.util.UUID

typealias UserID = UUID

data class User(
    val id: UserID,
    val username: String,
    val email: String,
    val hasEmailVerfied: Boolean
)
