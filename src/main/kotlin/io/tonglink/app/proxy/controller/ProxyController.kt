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
    val notificationService: PushNotificationService
) {

    @GetMapping("/{uuId}/{linkId}")
    fun proxyLink(request: HttpServletRequest,
                  @RequestHeader("X-Real-IP", required = false) realIp: String?,
                  @RequestHeader("X-Forwarded-For", required = false) forwardedFor: String?,
                  @RequestHeader("X-Is-Bot", required = false, defaultValue = "0") isBot: String,
                  @RequestHeader("X-Is-Preview", required = false, defaultValue = "0") isPreview: String,
                  @PathVariable(name = "uuId") uuId: String,
                  @PathVariable(name = "linkId") linkId: Long) : String {
        println("uuId = " + uuId);
        println("linkId = " + linkId);
        val link = proxyService.getRedirectLink(linkId)

        // 기간이 만료되었는지 확인
        if(link.isEndDatePassed()) {
            throw ExpirationException(ResultCode.EXPRIRATION)
        }


        val user = userService.getUserInfo(link.userKey)
        if(user.endPoint != null && user.p256dh != null && user.auth != null && user.isPushEnabled) {
            notificationService.sendPushNotification(Subscription(user.endPoint, Subscription.Keys(user.p256dh, user.auth)), link.title)
        }

        if(isBotVisit(isBot)) {
            println("Bot@@@@")
        }

        if(isPreviewVisit(isPreview)) {
            println("Preview@@@@")
        }

        // 방문 데이터 수집
        val userIp = forwardedFor?.split(",")?.firstOrNull() ?: realIp ?: "Unknown IP"
        val userAgent = request.getHeader("User-Agent")
        val referrer = request.getHeader("Referer")
        proxyService.saveVisit(link.id!!, link.userKey, userIp, userAgent, referrer)

        return "redirect:${link.originUrl}"
    }

    private fun isBotVisit(isBot: String) : Boolean {
        return isBot == "1"
    }

    private fun isPreviewVisit(isPreview: String) : Boolean {
        return isPreview == "1"
    }

}