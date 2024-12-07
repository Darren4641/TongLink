package io.tonglink.app.link.controller

import com.example.kopring.common.response.BaseResponse
import io.tonglink.app.link.dto.CreateLinkDto
import io.tonglink.app.link.dto.LinkDto
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

}