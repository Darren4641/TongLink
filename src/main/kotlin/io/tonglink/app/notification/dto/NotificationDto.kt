package io.tonglink.app.notification.dto

import nl.martijndwars.webpush.Subscription

class NotificationDto (
    val uuId: String,
) : Subscription()