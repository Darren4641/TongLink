package io.tonglink.app.link.repository

import com.querydsl.core.QueryResults
import com.querydsl.core.Tuple
import com.querydsl.jpa.impl.JPAQueryFactory
import io.tonglink.app.link.dto.LinkDto
import io.tonglink.app.link.entity.Link
import io.tonglink.app.link.entity.QLink.link
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable


class LinkRepositoryDslImpl (
    private val queryFactory: JPAQueryFactory
) : LinkRepositoryDsl {
    override fun getMyTongLink(uuId: String, pageable: Pageable): Page<LinkDto> {
        val results = queryFactory
            .select(link)
            .from(link)
            .where(link.userKey.eq(uuId))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetchResults()

        val content = results.results.map {
            LinkDto(
                title = it.title,
                originUrl = it.originUrl,
                proxyUrl = it.proxyUrl,
                endDate = it.endDate,
                thumbnailUrl = it.thumbnailUrl ?: "https://app.tonglink.site/images/app_logo.png"
            )
        }
        return PageImpl(content, pageable, results.total)
    }
}