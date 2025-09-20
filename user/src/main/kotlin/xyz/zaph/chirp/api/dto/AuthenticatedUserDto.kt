package xyz.zaph.chirp.api.dto

data class AuthenticatedUserDto(
    val user: UserDto,
    val accesToken: String,
    val refreshToken: String
)
