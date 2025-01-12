package io.tonglink.app.link.service

import com.example.kopring.common.status.ResultCode
import io.tonglink.app.common.dto.SimplePageImpl
import io.tonglink.app.common.exception.TongLinkException
import io.tonglink.app.common.util.CacheUtil
import io.tonglink.app.config.cache.Cacheable
import io.tonglink.app.config.cache.IgnoreCache
import io.tonglink.app.config.cache.RedisKey
import io.tonglink.app.link.dto.*
import io.tonglink.app.link.entity.Link
import io.tonglink.app.link.repository.LinkRepository
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class LinkService (
    val linkRepository: LinkRepository,
    val googleSafeService: GoogleSafeService,
    val cacheUtil: CacheUtil
) {

    fun createLink(createLinkDto: CreateLinkDto) : String {
        val isSafeUrl = googleSafeService.isSafeUrl(createLinkDto.originUrl)

        if(!isSafeUrl) {
            throw TongLinkException(ResultCode.NO_SAFE_URL)
        }

        val savedLink = linkRepository.save(Link(
            userKey = createLinkDto.uuId,
            title = createLinkDto.title,
            originUrl = createLinkDto.originUrl,
            proxyUrl = "https://app.tonglink.site/proxy/" + createLinkDto.uuId,
            color = createLinkDto.color,
            thumbnailUrl = getThumbnail(createLinkDto.originUrl),
            isExposure = createLinkDto.isExposure
        ))
        return savedLink.proxyUrl
    }

    @Transactional
    fun updateLink(updateLinkDto: UpdateLinkDto) : String {
        val targetLink = linkRepository.findByIdAndUserKey(updateLinkDto.id, updateLinkDto.uuId) ?: throw TongLinkException(ResultCode.ERROR)
        targetLink.updateLink(updateLinkDto)

        return targetLink.proxyUrl
    }

    @Transactional
    fun deleteLink(deleteLinkDto: DeleteLinkDto) : String {
        val targetLink = linkRepository.findByIdAndUserKey(deleteLinkDto.id, deleteLinkDto.uuId) ?: throw TongLinkException(ResultCode.ERROR)
        linkRepository.delete(targetLink)

        return targetLink.proxyUrl
    }

    @Transactional
    fun extendLink(extendLinkDto: ExtendLinkDto): String {
        val targetLink = linkRepository.findByIdAndUserKey(extendLinkDto.id, extendLinkDto.uuId)
            ?: throw TongLinkException(ResultCode.ERROR)

        // 현재 시간을 LocalDateTime으로 가져오기
        val now = LocalDateTime.now()

        // endDate 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val endDate = LocalDateTime.parse(targetLink.endDate, formatter)

        // 오늘 날짜와 endDate 간의 차이 계산 (일 단위)
        val daysBetween = ChronoUnit.DAYS.between(now.toLocalDate(), endDate.toLocalDate())

        if (daysBetween <= 3) {
            // 1주일 연장
            val extendedDate = endDate.plusWeeks(1)
            targetLink.endDate = extendedDate.format(formatter) // String으로 저장
        }

        return extendLinkDto.uuId
    }

    @Cacheable(key1 = RedisKey.TONGLINK_HOME, ttl = 300)
    fun getMyTongLink(uuId: String, pageable: Pageable, @IgnoreCache ignoreCache: Boolean) : SimplePageImpl<LinkDto> {
        return linkRepository.getMyTongLink(uuId, pageable)
    }

    @Cacheable(key1 = RedisKey.TONGLINK_RANK, ttl = 300)
    fun getPopularTongLink(pageable: Pageable) : SimplePageImpl<PopularLinkDto> {
        return linkRepository.getPopularTongLink(pageable)
    }

    @Transactional
    fun updateOrderTongLink(uuId: String, updateOrderLinkDto: List<UpdateOrderLinkDto>) : String {
        linkRepository.updateOrderTongLink(uuId, updateOrderLinkDto)
        cacheUtil.deleteCacheByPrefix(RedisKey.TONGLINK_HOME.name + ":" + uuId)
        return "OK"
    }

    fun statistics(uuId: String) : List<StatisticsLinkDto> {
        return linkRepository.getStatistics(uuId)
    }

    @Transactional(readOnly = true)
    fun getMyTongLinkTotalCount(uuId: String) : LinkTotalCount {
        return linkRepository.getMyTongLinkTotalCount(uuId)
    }


    /**
     * 주어진 URL에서 Open Graph 데이터를 추출
     * @param url 대상 URL
     * @return 썸네일 URL (og:image) 또는 기본 이미지
     */
    private fun getThumbnail(url: String): String {
        return try {
            // URL로부터 HTML 문서 로드
            val document: Document = Jsoup.connect(url)
                .timeout(2000) // 5초 타임아웃
                .get()

            // Open Graph 태그에서 og:image 속성 추출
            val thumbnailUrl: String? = document.select("meta[property=og:image]").attr("content")

            // 썸네일 URL 반환 또는 기본 이미지 제공
            if (thumbnailUrl.isNullOrBlank()) {
                "https://app.tonglink.site/images/app_logo.png"
            } else {
                thumbnailUrl
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 실패 시 기본 이미지 반환
            "https://app.tonglink.site/images/app_logo.png"
        }
    }

    private fun getFiveDaysAgo(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul")) // 현재 날짜/시간
        val fiveDaysAgo = now.minusDays(5) // 5일 전
        return fiveDaysAgo.format(formatter)
    }
}