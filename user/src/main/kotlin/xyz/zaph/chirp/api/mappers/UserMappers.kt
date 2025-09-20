package xyz.zaph.chirp.api.mappers

import xyz.zaph.chirp.api.dto.AuthenticatedUserDto
import xyz.zaph.chirp.api.dto.UserDto
import xyz.zaph.chirp.domain.model.AuthenticatedUser
import xyz.zaph.chirp.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto() = AuthenticatedUserDto(
    user = user.toUserDto(),
    accesToken = accessToken,
    refreshToken = refreshToken
)

fun User.toUserDto() = UserDto(
    id = id,
    email = email,
    username = username,
    hasVerifiedEmail = hasEmailVerfied
)