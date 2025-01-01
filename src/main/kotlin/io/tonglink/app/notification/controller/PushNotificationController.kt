package io.tonglink.app.notification.controller

import io.tonglink.app.notification.dto.NotificationDto
import io.tonglink.app.notification.service.PushNotificationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/push")
class PushNotificationController (
    val pushNotificationService: PushNotificationService
) {

    @PostMapping("/subscribe")
    fun popularTongLink(@RequestBody notificationDto: NotificationDto) {
        pushNotificationService.subscribe(notificationDto)
    }

}