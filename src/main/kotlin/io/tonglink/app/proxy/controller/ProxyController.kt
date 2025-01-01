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
        if(user.email != null) {
            notificationService.sendPushNotification(Subscription(user.endPoint, Subscription.Keys(user.p256dh, user.auth)), "누군가 링크에 접속했습니다!")
        }

        // 방문 데이터 수집
        val userIp = request.remoteAddr
        val userAgent = request.getHeader("User-Agent")
        val referrer = request.getHeader("Referer")
        proxyService.saveVisit(link.id!!, link.userKey, userIp, userAgent, referrer)

        return "redirect:${link.originUrl}"
    }

}