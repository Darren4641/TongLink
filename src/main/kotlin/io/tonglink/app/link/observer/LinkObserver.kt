package io.tonglink.app.link.observer

import io.tonglink.app.common.util.CacheUtil
import io.tonglink.app.config.cache.RedisKey
import io.tonglink.app.link.entity.Link
import jakarta.persistence.PostPersist
import jakarta.persistence.PostRemove
import jakarta.persistence.PostUpdate

class LinkObserver(private val cacheUtil: CacheUtil) {


    @PostPersist
    fun afterInsert(link: Link) {
        refreshCache(link)
    }

    @PostUpdate
    fun afterUpdate(link: Link) {
        refreshCache(link)
    }

    @PostRemove
    fun afterRemove(link: Link) {
        refreshCache(link)
    }

    private fun refreshCache(link: Link) {
        // 메인 페이지 캐시 삭제
        cacheUtil.deleteCache(RedisKey.TONGLINK_HOME, link.userKey)

        // 사연 메인 캐시 삭제
        //cacheUtil.deleteCacheByPrefix(RedisConst.WEB_MAIN_COLLECTION)

        // 사연 상세 캐시 삭제
        /*cacheUtil.deleteCacheByPrefix(
            cacheUtil.getCacheKey(RedisConst.WEB_COLLECTION_DETAIL, story.seq.toString())
        )*/

        // 사연 소식 캐시 삭제
        /*cacheUtil.deleteCacheByPrefix(
            cacheUtil.getCacheKey(RedisConst.WEB_COLLECTION_NOTICE, story.seq.toString())
        )*/
    }
}