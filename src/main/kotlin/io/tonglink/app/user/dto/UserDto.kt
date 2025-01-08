package io.tonglink.app.user.dto

class UserDto (
    val uuId: String
)

class PushNotificationSettingRequest (
    val uuId: String,
    val isPushEnabled: Boolean
)

class UserDataDto (
    val email: String?,
    val uuId: String,
    val endPoint: String?,
    val p256dh: String?,
    val auth: String?,
    val isPushEnabled: Boolean,
    val createdDate: String
)