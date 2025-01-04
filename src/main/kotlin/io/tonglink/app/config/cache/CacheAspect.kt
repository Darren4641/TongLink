package io.tonglink.app.config.cache

import io.tonglink.app.common.util.CacheUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import java.lang.reflect.Type

@Aspect
@Component
class CacheAspect(
    private val cacheUtil: CacheUtil
) {

    @Around("@annotation(cacheable)")
    @Throws(Throwable::class)
    fun cacheAround(joinPoint: ProceedingJoinPoint, cacheable: Cacheable): Any {
        // 기본 캐시 키를 어노테이션에서 가져옴
        val key1 = cacheable.key1
        var key2 = cacheable.key2
        val ttl = cacheable.ttl

        // 파라미터
        val args = joinPoint.args

        // 동적 키를 만들기 위해 메소드 파라미터를 기반으로 키2 생성
        if (key2.isBlank() && args.isNotEmpty()) {
            key2 = args.filter { it is String || it is Number }
                .joinToString(":") { it.toString() }
        }

        // 메서드의 리턴 타입 가져오기
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val returnType: Type = method.genericReturnType

        // 캐시를 무시하고 데이터 조회할지 여부
        var ignoreCache = false
        val parameterAnnotations = method.parameterAnnotations
        if (args.isNotEmpty() && parameterAnnotations.isNotEmpty()) {
            for (i in args.indices) {
                if (args[i] is Boolean && args[i] as Boolean) {
                    for (annotation in parameterAnnotations[i]) {
                        if (annotation is IgnoreCache) {
                            ignoreCache = true
                            break
                        }
                    }
                }
                if (ignoreCache) break
            }
        }

        // 캐시 처리
        return cacheUtil.getCache(
            key1 = key1,
            key2 = key2,
            dataSupplier = {
                joinPoint.proceed()
            },
            ttl = ttl,
            returnType = returnType,
            ignoreCache = ignoreCache
        )
    }
}