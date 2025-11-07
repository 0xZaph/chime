package xyz.zaph.chirp.infra.rate_limiting

import com.sun.net.httpserver.Request
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import xyz.zaph.chirp.infra.config.NginxConfig
import java.net.Inet4Address
import java.net.Inet6Address

@Component
class IpResolver(
    private val nginx: NginxConfig
) {

    companion object {
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "127.0.0.0/8",
            "::1/128",
            "fc00::/7",
            "fe80::/10"
        ).map { IpAddressMatcher(it) }

        private val INVALID_IPS = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )
    }

    private val logger = LoggerFactory.getLogger(IpResolver::class.java)

    private val trustedMatchers: List<IpAddressMatcher> = nginx.
        trustedIps
        .filter { it.isNotBlank() }
        .map { proxy ->
            val cidr = when {
                proxy.contains("/") -> proxy
                proxy.contains(":") -> "$proxy/128"
                else -> "$proxy/32"
            }
            IpAddressMatcher(cidr)
        }

    fun getClient(request: HttpServletRequest): String {
        val remoteAddress = request.remoteAddr

        if (!isFromTrustedProxy(remoteAddress)) {
            if (nginx.requireProxy) {
                logger.warn("Direct connection with remote address $remoteAddress")
                throw SecurityException("No valid client IP in proxy header")
            }
            return remoteAddress
        }

        val clientIP  = extractFromXRealIp(request, remoteAddress)

        if (clientIP == null) {
            logger.warn("No valid client IP in proxy header")
            if (nginx.requireProxy) {
                throw SecurityException("No valid client IP in proxy header")
            }
        }

        return clientIP ?: throw SecurityException("No valid client IP in proxy header")
    }

    private fun extractFromXRealIp(request: HttpServletRequest, proxyIp: String): String? {
        return request.getHeader("X-Real-IP")?.let { header ->
            validateAndNormalizeIp(header, "X-Real-IP", proxyIp)
        }
    }

    private fun validateAndNormalizeIp(ip: String, headerName: String, proxyIp: String): String? {
        val trimmedIp = ip.trim()

        if(trimmedIp.isBlank() || INVALID_IPS.contains(trimmedIp)) {
            logger.debug("Invalid IP in $headerName: $ip from proxy $proxyIp")
            return null
        }

        return try {
            val inetAddr = when {
                trimmedIp.contains(":") -> Inet6Address.getByName(trimmedIp)
                trimmedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) ->
                    Inet4Address.getByName(trimmedIp)
                else -> {
                    logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp")
                    return null
                }
            }

            if(isPrivateIp(inetAddr.hostAddress)) {
                logger.debug("Private IP in $headerName: $trimmedIp from proxy $proxyIp")
            }

            inetAddr.hostAddress
        } catch(e: Exception) {
            logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp", e)
            null
        }
    }

    private fun isPrivateIp(ip: String): Boolean {
        return PRIVATE_IP_RANGES.any {it.matches(ip)}
    }

    private fun isFromTrustedProxy(ip: String): Boolean {
        return trustedMatchers.any { it.matches(ip) }
    }
}