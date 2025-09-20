package xyz.zaph.chirp.api.controllers

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xyz.zaph.chirp.api.dto.AuthenticatedUserDto
import xyz.zaph.chirp.api.dto.ChangePasswordRequest
import xyz.zaph.chirp.api.dto.EmailRequest
import xyz.zaph.chirp.api.dto.LoginRequest
import xyz.zaph.chirp.api.dto.RefreshRequest
import xyz.zaph.chirp.api.dto.RegisterRequest
import xyz.zaph.chirp.api.dto.ResetPasswordRequest
import xyz.zaph.chirp.api.dto.UserDto
import xyz.zaph.chirp.api.mappers.toAuthenticatedUserDto
import xyz.zaph.chirp.api.mappers.toUserDto
import xyz.zaph.chirp.service.AuthService
import xyz.zaph.chirp.service.EmailVerificationService
import xyz.zaph.chirp.service.PasswordResetService

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ) : UserDto {
        val user = authService.register(request.email, request.username, request.password)
        return user.toUserDto()
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest
    ) : AuthenticatedUserDto {
        val user = authService.login(request.email, request.password)
        return user.toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody request: RefreshRequest
    ) : AuthenticatedUserDto {
        val user = authService.refresh(request.refreshToken)
        return user.toAuthenticatedUserDto()
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String,
    ) {
        emailVerificationService.verifyEmail(token)
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody request: EmailRequest) {
        passwordResetService.requestPasswordReset(request.email)
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest
    ) {
        passwordResetService.resetPassword(request.token, request.newPassword)
    }


    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest
    ) {
        // TODO: Extract user from JWT
    }
}