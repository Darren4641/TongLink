package io.tonglink.app.proxy.controller

import com.example.kopring.common.status.ResultCode
import io.tonglink.app.common.exception.ExpirationException
import io.tonglink.app.notification.service.PushNotificationService
import io.tonglink.app.proxy.service.ProxyService
import io.tonglink.app.user.service.UserService
import jakarta.servlet.http.HttpServletRequest
import nl.martijndwars.webpush.Subscription
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/proxy")
class ProxyController (
    val proxyService: ProxyService,
    val userService: UserService,

) {

    @GetMapping("/{uuId}/{linkId}")
    fun proxyLink(request: HttpServletRequest,
                  @RequestHeader("X-Real-IP", required = false) realIp: String?,
                  @RequestHeader("X-Forwarded-For", required = false) forwardedFor: String?,
                  @RequestHeader("X-Is-Bot", required = false, defaultValue = "0") isBot: String,
                  @RequestHeader("X-Is-Preview", required = false, defaultValue = "0") isPreview: String,
                  @PathVariable(name = "uuId") uuId: String,
                  @PathVariable(name = "linkId") linkId: Long) : String {
        val link = proxyService.getRedirectLink(linkId)

        // 기간이 만료되었는지 확인
        if(link.isEndDatePassed()) {
            throw ExpirationException(ResultCode.EXPRIRATION)
        }

        val user = userService.getUserInfo(link.userKey)


        if(!isBotVisit(isBot) && isPreviewVisit(isPreview)) {
            proxyService.visitDataCollection(request, link, user, realIp, forwardedFor)
        }


        return "redirect:${link.originUrl}"
    }

    private fun isBotVisit(isBot: String) : Boolean {
        return isBot == "1"
    }

    private fun isPreviewVisit(isPreview: String) : Boolean {
        return isPreview == "1"
    }

}