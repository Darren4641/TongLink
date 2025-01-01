package io.tonglink.app.user.dto

class UserDto (
    val uuId: String
)

class UserDataDto (
    val email: String?,
    val uuId: String,
    val endPoint: String,
    val p256dh: String,
    val auth: String
)