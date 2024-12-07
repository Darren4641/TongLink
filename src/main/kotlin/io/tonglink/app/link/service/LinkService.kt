package io.tonglink.app.link.service

import io.tonglink.app.common.dto.SimplePageImpl
import io.tonglink.app.link.dto.CreateLinkDto
import io.tonglink.app.link.dto.LinkDto
import io.tonglink.app.link.entity.Link
import io.tonglink.app.link.repository.LinkRepository
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class LinkService (
    val linkRepository: LinkRepository
) {

    fun createLink(createLinkDto: CreateLinkDto) : String {
        val savedLink = linkRepository.save(Link(
            userKey = createLinkDto.uuId,
            title = createLinkDto.title,
            originUrl = createLinkDto.originUrl,
            proxyUrl = "https://app.tonglink.site/" + createLinkDto.uuId,
            thumbnailUrl = getThumbnail(createLinkDto.originUrl)
        ))
        return savedLink.proxyUrl
    }

    fun getMyTongLink(uuId: String, pageable: Pageable) : Page<LinkDto> {
        return linkRepository.getMyTongLink(uuId, pageable)
    }

    /**
     * 주어진 URL에서 Open Graph 데이터를 추출
     * @param url 대상 URL
     * @return 썸네일 URL (og:image) 또는 기본 이미지
     */
    fun getThumbnail(url: String): String {
        return try {
            // URL로부터 HTML 문서 로드
            val document: Document = Jsoup.connect(url)
                .timeout(5000) // 5초 타임아웃
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
}