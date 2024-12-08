package io.tonglink.app.proxy.controller

import com.example.kopring.common.status.ResultCode
import io.tonglink.app.common.exception.ExpirationException
import io.tonglink.app.proxy.service.ProxyService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/proxy")
class ProxyController (
    val proxyService: ProxyService
) {

    @GetMapping("/{uuId}/{linkId}")
    fun proxyLink(request: HttpServletRequest,
                  @PathVariable(name = "uuId") uuId: String,
                  @PathVariable(name = "linkId") linkId: Long) : String {
        val link = proxyService.getRedirectLink(uuId, linkId)

        // 기간이 만료되었는지 확인
        if(link.isEndDatePassed()) {
            throw ExpirationException(ResultCode.EXPRIRATION)
        }

        // 방문 데이터 수집
        val userIp = request.remoteAddr
        val userAgent = request.getHeader("User-Agent")
        val referrer = request.getHeader("Referer")
        proxyService.saveVisit(link.id!!, uuId, userIp, userAgent, referrer)

        return "redirect:${link.originUrl}"
    }

}