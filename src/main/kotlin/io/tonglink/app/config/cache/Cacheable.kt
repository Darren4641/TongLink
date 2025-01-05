package io.tonglink.app.config.cache

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cacheable(
    val key1: RedisKey, // 캐시 키의 첫 번째 값
    val key2: String = "", // 캐시 키의 두 번째 값 (옵션)
    val ttl: Long = 3600 // 캐시 TTL (기본 1시간)
)