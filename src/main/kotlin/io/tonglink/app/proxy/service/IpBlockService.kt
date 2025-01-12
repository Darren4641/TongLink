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

        // 요청 키가 없을 경우 초기화
        val isNewKey = redisTemplate.opsForValue().setIfAbsent(requestKey, "1", requestIntervalSeconds, TimeUnit.SECONDS)

        if (isNewKey == true) {
            // 키가 새로 생성된 경우, 값이 이미 1로 설정됨
            return
        }

        // 기존 키의 요청 횟수 증가
        val currentCount = redisTemplate.opsForValue().get(requestKey)?.toIntOrNull() ?: 0

        if (currentCount >= maxRequests) {
            // 요청 초과: IP 블록 처리
            blockIp(ip)
        } else {
            redisTemplate.opsForValue().increment(requestKey)
            // expire 호출 제거 (기존 TTL 유지)
        }
    }


    private fun blockIp(ip: String) {
        val blockKey = RedisKey.BLOCKED_IP.withParams(ip)
        redisTemplate.opsForValue().set(blockKey, "blocked", blockDurationMinutes, TimeUnit.MINUTES)
    }
}