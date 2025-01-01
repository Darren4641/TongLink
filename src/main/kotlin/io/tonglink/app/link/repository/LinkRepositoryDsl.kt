package io.tonglink.app.link.repository

import io.tonglink.app.link.dto.LinkDto
import io.tonglink.app.link.dto.StatisticsLinkDto
import io.tonglink.app.link.dto.UpdateOrderLinkDto
import io.tonglink.app.link.entity.Link
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


interface LinkRepositoryDsl {

    fun getMyTongLink(uuId: String, pageable: Pageable) : Page<LinkDto>

    fun updateOrderTongLink(uuId: String, updateOrderLinkDto: List<UpdateOrderLinkDto>)

    fun findRedirectLink(linkId: Long) : Link

    fun getStatistics(uuId: String) : List<StatisticsLinkDto>

}