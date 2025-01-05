package io.tonglink.app.link.repository

import io.tonglink.app.common.dto.SimplePageImpl
import io.tonglink.app.link.dto.*
import io.tonglink.app.link.entity.Link
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


interface LinkRepositoryDsl {

    fun getMyTongLink(uuId: String, pageable: Pageable) : SimplePageImpl<LinkDto>

    fun getPopularTongLink(pageable: Pageable) : SimplePageImpl<PopularLinkDto>

    fun updateOrderTongLink(uuId: String, updateOrderLinkDto: List<UpdateOrderLinkDto>)

    fun findRedirectLink(linkId: Long) : Link

    fun getStatistics(uuId: String) : List<StatisticsLinkDto>

    fun getMyTongLinkTotalCount(uuId: String) : LinkTotalCount

}