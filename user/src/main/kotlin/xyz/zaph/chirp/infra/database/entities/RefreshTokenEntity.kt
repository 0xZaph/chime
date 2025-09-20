package xyz.zaph.chirp.infra.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import xyz.zaph.chirp.domain.model.UserID
import java.time.Instant

@Entity
@Table(
    name = "refresh_tokens",
    schema = "user_service",
    indexes = [
        Index(name = "idx_refresh_tokens_user_id", columnList = "userid"),
        Index(name = "idx_refresh_tokens_user_token_id", columnList = "userid,hashed_token")
    ]
)
class RefreshTokenEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var userID: UserID,

    @Column(nullable = false)
    var expiresAt: Instant,

    @Column(nullable = false)
    var hashedToken: String,

    @CreationTimestamp
    var createdAt: Instant = Instant.now()
)