package io.tonglink.app.config.cache

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(factory)

        // Key/Value 직렬화 설정
        template.keySerializer = StringRedisSerializer()

        return template
    }
}