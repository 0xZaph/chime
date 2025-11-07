package xyz.zaph.chirp.domain.exception

class RateLimitException(
    val resetsInSeconds: Long,
): RuntimeException("Rate limit exceeded. Try again in $resetsInSeconds seconds.")