package io.tonglink.app.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.tonglink.app.config.cache.RedisKey
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.lang.reflect.Type
import java.time.Duration

@Component
class CacheUtil(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(CacheUtil::class.java)

    fun <T> getCache(
        key1: String,
        key2: String,
        dataSupplier: () -> T,
        ttl: Long,
        returnType: Type,
        ignoreCache: Boolean = false
    ): T {
        val cacheKey = getCacheKey(key1, key2)

        if (!ignoreCache) {
            val cachedData = redisTemplate.opsForValue()[cacheKey]
            if (!cachedData.isNullOrEmpty()) {
                return try {
                    objectMapper.readValue(cachedData, objectMapper.constructType(returnType))
                } catch (e: Exception) {
                    throw RuntimeException("Failed to deserialize cached data - $cacheKey", e)
                }
            }
        }

        val data = dataSupplier()
        setCache(key1, key2, ttl, data!!)
        return data
    }

    fun setCache(key1: String, key2: String, ttl: Long, data: Any) {
        val cacheKey = getCacheKey(key1, key2)
        try {
            val jsonData = objectMapper.writeValueAsString(data)
            redisTemplate.opsForValue().set(cacheKey, jsonData, Duration.ofSeconds(ttl))
        } catch (e: Exception) {
            log.error("Failed to serialize data for caching - $cacheKey", e)
        }
    }

    private fun getCacheKey(key1: String, key2: String): String {
        return if (key2.isBlank()) key1 else "$key1:$key2"
    }

    fun deleteCache(key1: RedisKey, key2: String): Boolean {
        val cacheKey = getCacheKey(key1.name, key2)
        return try {
            redisTemplate.delete(cacheKey)
            log.debug("Cache deleted - $cacheKey")
            true
        } catch (e: Exception) {
            log.error("Failed to delete cache - $cacheKey", e)
            false
        }
    }

    fun deleteCacheByPrefix(prefix: String): Boolean {
        if (prefix.isBlank()) {
            log.error("Prefix cannot be blank for cache deletion.")
            return false
        }

        return try {
            val keys = redisTemplate.keys("$prefix*")
            println("delete Key = " + keys)
            keys?.let { redisTemplate.delete(it) }
            log.debug("Caches deleted by prefix - $prefix")
            true
        } catch (e: Exception) {
            log.error("Failed to delete caches by prefix - $prefix", e)
            false
        }
    }
}