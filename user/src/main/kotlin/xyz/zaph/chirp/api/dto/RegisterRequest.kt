package xyz.zaph.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import xyz.zaph.chirp.api.util.Password

data class RegisterRequest @JsonCreator constructor(
    @field:Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters long")
    @JsonProperty("username")
    val username: String,

    @field:Email(message = "Please provide a valid email address")
    @JsonProperty("email")
    val email: String,

    @field:Password
    @JsonProperty("password")
    val password: String,
)
