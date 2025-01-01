package io.tonglink.app.link.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import io.tonglink.app.link.dto.LinkDto
import io.tonglink.app.link.dto.PopularLinkDto
import io.tonglink.app.link.dto.StatisticsLinkDto
import io.tonglink.app.link.dto.UpdateOrderLinkDto
import io.tonglink.app.link.entity.Link
import io.tonglink.app.link.entity.QLink.link
import io.tonglink.app.link.entity.QVisit.visit
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


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
            .orderBy(link.order.asc(), link.createdDate.desc())
            .fetchResults()

        val content = results.results.map {
            LinkDto(
                id = it.id!!,
                title = it.title,
                originUrl = it.originUrl,
                proxyUrl = it.proxyUrl,
                endDate = it.endDate,
                thumbnailUrl = it.thumbnailUrl ?: "https://app.tonglink.site/images/app_logo.png",
                color = it.color,
                order = it.order,
                isExposure = it.isExposure,
                count = 0
            )
        }
        return PageImpl(content, pageable, results.total)
    }

    override fun getPopularTongLink(pageable: Pageable): Page<PopularLinkDto> {
        val todayStart = LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val todayStartString = todayStart.format(formatter)

        val results = queryFactory
            .select(
                link.id,
                link.title,
                link.proxyUrl,
                link.thumbnailUrl,
                visit.id.count()
            )
            .from(link)
            .leftJoin(visit).on(visit.linkId.eq(link.id))
            .where(link.isExposure.eq(true)
                .and(link.endDate.gt(todayStartString)))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(visit.id.count().desc())
            .groupBy(link.id)
            .fetchResults()

        val content = results.results.map {
            PopularLinkDto(
                id = it.get(link.id)!!,
                title = it.get(link.title)!!,
                proxyUrl = it.get(link.proxyUrl)!!,
                thumbnailUrl = it.get(link.thumbnailUrl) ?: "https://app.tonglink.site/images/app_logo.png",
                count = it.get(visit.id.count())!!
            )
        }
        return PageImpl(content, pageable, results.total)
    }

    override fun updateOrderTongLink(uuId: String, updateOrderLinkDto: List<UpdateOrderLinkDto>) {
        // 첫 번째 케이스 생성 (CaseBuilder -> CaseBuilder.Cases)
        var cases = CaseBuilder()
            .`when`(link.id.eq(updateOrderLinkDto[0].id))
            .then(updateOrderLinkDto[0].order)

        // 두 번째 케이스부터는 cases에 체이닝
        for (i in 1 until updateOrderLinkDto.size) {
            val request = updateOrderLinkDto[i]
            cases = cases.`when`(link.id.eq(request.id)).then(request.order)
        }

        val caseExpression = cases.otherwise(link.order)

        queryFactory.update(link)
            .set(link.order, caseExpression)
            .where(link.userKey.eq(uuId)
                .and(link.id.`in`(updateOrderLinkDto.map { it.id })))
            .execute()
    }

    override fun findRedirectLink(linkId: Long) : Link {
        return queryFactory
            .select(link)
            .from(link)
            .where(link.id.eq(linkId))
            .fetchFirst()!!
    }

    override fun getStatistics(uuId: String) : List<StatisticsLinkDto> {
        val today = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        val fiveDaysAgo = today.minusDays(5)

        return queryFactory
            .select(
                Projections.constructor(
                    StatisticsLinkDto::class.java,
                    link.id,
                    link.title,
                    link.color,
                    Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", visit.createdDate), // 명시적으로 문자열 반환
                    visit.id.count()
                )
            )
            .from(visit)
            .join(link).on(visit.linkId.eq(link.id))
            .where(
                link.userKey.eq(uuId)
                    .and(
                        Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", visit.createdDate)
                            .between(fiveDaysAgo.toString(), today.toString())
                    )
            )
            .groupBy(link.id, link.title, link.color, Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", visit.createdDate))
            .orderBy(Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", visit.createdDate).asc())
            .fetch()
    }


}