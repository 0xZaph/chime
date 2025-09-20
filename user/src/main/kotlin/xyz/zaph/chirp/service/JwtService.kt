package xyz.zaph.chirp.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import xyz.zaph.chirp.domain.exception.InvalidTokenException
import xyz.zaph.chirp.domain.model.UserID
import java.util.Date
import java.util.UUID
import kotlin.io.encoding.Base64

@Service
class JwtService(
    @param:Value("\${jwt.secret}") private val secret: String,
    @param:Value("\${jwt.expiration-minutes}") private val expirationMinutes: Int,
) {

    private val secretKey = Keys.hmacShaKeyFor(
        Base64.decode(secret)
    )

    private val accesTokenValidityMS = expirationMinutes * 60 * 1000L

    fun generateAccessToken(userID: UserID): String {
        return generateToken(userID, "access", accesTokenValidityMS)
    }

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokentype = claims["type"] as? String ?: return false
        return tokentype == "access"
    }

    val refreshTokenValidityMS = 30 * 24 * 60 * 60 * 1000L // 30 days

    fun generateRefreshToken(userID: UserID): String {
        return generateToken(userID, "refresh", refreshTokenValidityMS)
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokentype = claims["type"] as? String ?: return false
        return tokentype == "refresh"
    }

    fun getUserIdFromToken(token: String): UserID {
        val claims = parseAllClaims(token) ?: throw InvalidTokenException(
            "The attached JWT token is not valid"
        )

        return UUID.fromString(claims.subject)
    }

    private fun generateToken(
        userID: UserID,
        type: String,
        expiry: Long,
    ): String {
        val now = Date()
        val expirationDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userID.toString())
            .claim("type", type)
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if (token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else token

        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}