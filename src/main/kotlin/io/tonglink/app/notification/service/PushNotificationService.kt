package io.tonglink.app.notification.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.tonglink.app.notification.dto.NotificationDto
import io.tonglink.app.user.repository.UserRepository
import nl.martijndwars.webpush.Notification
import nl.martijndwars.webpush.PushService
import nl.martijndwars.webpush.Subscription
import org.apache.http.HttpResponse
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Service
class PushNotificationService (
    val pushService: PushService,
    val userRepository: UserRepository
) {

    fun subscribe(notificationDto: NotificationDto) {
        userRepository.findByUUID(notificationDto.uuId)?.let {
            it.subscribe(notificationDto)
            userRepository.save(it)
        }
    }

    fun sendPushNotification(subscription: Subscription, title: String, serverBaseUrl: String) {

        val payloadData = mapOf(
            "title" to "통통🎉",
            "body" to "누군가 ${title} 에 방문했습니디!",
            "icon" to "$serverBaseUrl/images/app_logo.png"
        )

        try {
            val payloadJson = jacksonObjectMapper().writeValueAsString(payloadData)

            val notification = Notification(
                subscription,
                payloadJson
            )
            val response: HttpResponse = pushService.send(notification)
            val statusCode = response.statusLine.statusCode;
            if (statusCode != 201) {
                println("PWA 알림 전송 실패")
                println("응답 메시지: ${response.entity.content.reader().readText()}")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}