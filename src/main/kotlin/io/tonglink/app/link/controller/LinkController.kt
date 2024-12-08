package io.tonglink.app.link.controller

import com.example.kopring.common.response.BaseResponse
import io.tonglink.app.link.dto.*
import io.tonglink.app.link.service.LinkService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/link")
class LinkController (
    val linkService: LinkService
) {

    @PostMapping
    fun saveLink(@RequestBody createLinkDto: CreateLinkDto) : BaseResponse<String> {
        return BaseResponse(data = linkService.createLink(createLinkDto))
    }

    @GetMapping("/{uuId}")
    fun myTongLink(@PathVariable(name = "uuId") uuId: String,
                   @RequestParam(value = "limit", required = false, defaultValue = "5") limitParam: Int,
                   @RequestParam(value = "page", required = false, defaultValue = "0") pageParam: Int) : BaseResponse<Page<LinkDto>> {

        var limit = limitParam
        var page = pageParam
        if (limit <= 0) limit = 5
        if (page <= 0) page = 0
        val pageable: Pageable = PageRequest.of(page, limit)

        return BaseResponse(data = linkService.getMyTongLink(uuId, pageable))
    }

    @PostMapping("/{uuId}/update-order")
    fun updateOrderTongLink(@PathVariable(name = "uuId") uuId: String,
                            @RequestBody updateOrderLinkRequestDto: List<UpdateOrderLinkRequestDto>) : BaseResponse<String> {

        val orderList = updateOrderLinkRequestDto.map { UpdateOrderLinkDto(id = it.id.toLong(), order = it.order) }

        return BaseResponse(data = linkService.updateOrderTongLink(uuId, orderList))
    }

    @GetMapping("/{uuId}/statistics")
    fun tongLinkStatistics(@PathVariable(name = "uuId") uuId: String) : BaseResponse<List<StatisticsLinkDto>> {
        return BaseResponse(data = linkService.statistics(uuId))
    }
}