package io.tonglink.app.proxy.service

import io.tonglink.app.config.cache.RedisKey
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class IpBlockService(
    private val redisTemplate: StringRedisTemplate
) {

    private val blockDurationMinutes = 30L  // 차단 시간 (분)
    private val maxRequests = 20             // 허용 요청 수
    private val requestIntervalSeconds = 60L // 시간 범위 (초)

    fun isBlocked(ip: String): Boolean {
        val blockKey = RedisKey.BLOCKED_IP.withParams(ip)
        return redisTemplate.hasKey(blockKey) == true
    }

    fun incrementRequest(ip: String) {
        val requestKey = RedisKey.REQUEST_COUNT.withParams(ip)

        // 현재 요청 횟수 가져오기
        val currentCount = redisTemplate.opsForValue().get(requestKey)?.toIntOrNull() ?: 0

        if (currentCount >= maxRequests) {
            // 요청 초과: IP 블록 처리
            blockIp(ip)
        } else {
            // 요청 증가
            redisTemplate.opsForValue().increment(requestKey)
            redisTemplate.expire(requestKey, requestIntervalSeconds, TimeUnit.SECONDS)
        }
    }

    private fun blockIp(ip: String) {
        val blockKey = RedisKey.BLOCKED_IP.withParams(ip)
        redisTemplate.opsForValue().set(blockKey, "blocked", blockDurationMinutes, TimeUnit.MINUTES)
    }
}