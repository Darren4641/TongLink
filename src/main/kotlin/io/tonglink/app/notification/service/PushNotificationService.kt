package io.tonglink.app.notification.service

import io.tonglink.app.notification.dto.NotificationDto
import io.tonglink.app.user.repository.UserRepository
import nl.martijndwars.webpush.Encoding
import nl.martijndwars.webpush.Notification
import nl.martijndwars.webpush.PushService
import nl.martijndwars.webpush.Subscription
import org.apache.http.HttpResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

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

    fun sendPushNotification(subscription: Subscription, payload: String) {
        try {
            val notification = Notification(
                subscription,
                payload
            )
            val response: HttpResponse = pushService.send(notification, Encoding.AES128GCM)
            val statusCode = response.statusLine.statusCode;
            if (statusCode != 201) {
                println("PWA 알림 전송 실패")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}