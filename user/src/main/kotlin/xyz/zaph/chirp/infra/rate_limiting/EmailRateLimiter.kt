package xyz.zaph.chirp.infra.rate_limiting

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import xyz.zaph.chirp.domain.exception.RateLimitException

@Component
class EmailRateLimiter(
    private val redisTemplate: StringRedisTemplate
) {

    @Value("classpath:email_rate_limit.lua")
    lateinit var rateLimitResource: Resource

    private val rateLimitScript by lazy {
        val script = rateLimitResource.inputStream.bufferedReader().use { it.readText() }

        DefaultRedisScript(script, List::class.java as Class<List<Long>>)
    }
    companion object {
        private const val EMAIL_RATE_LIMIT_PREFIX = "rate_limit:email"
        private const val EMAIL_ATTEMPT_COUNT_PREFIX = "email_attempt_count"
    }

    fun withRateLimit(
        email: String,
        action: () -> Unit
    ) {
        val normalisedEmail = email.lowercase().trim()

        val rateLimitKey = "$EMAIL_RATE_LIMIT_PREFIX:$normalisedEmail"
        val attemptCountKey = "$EMAIL_ATTEMPT_COUNT_PREFIX:$normalisedEmail"

        val result = redisTemplate.execute(rateLimitScript, listOf(rateLimitKey, attemptCountKey))

        val attemptCount = result[0]
        val ttl = result[1]

        if (attemptCount == -1L) {
            throw RateLimitException(resetsInSeconds = ttl)
        }

        action()

    }
}