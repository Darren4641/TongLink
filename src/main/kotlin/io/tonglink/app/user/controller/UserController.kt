package io.tonglink.app.user.controller

import io.tonglink.app.common.response.BaseResponse
import io.tonglink.app.user.dto.PushNotificationSettingRequest
import io.tonglink.app.user.dto.UserDataDto
import io.tonglink.app.user.dto.UserDto
import io.tonglink.app.user.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController (
    val userService: UserService
) {

    @PostMapping
    fun enterTongLink(@RequestBody userDto: UserDto) : BaseResponse<String> {
        return BaseResponse(data = userService.saveUserIfNot(userDto, false))
    }

    @PostMapping("/pwa")
    fun enterTongLinkPWA(@RequestBody userDto: UserDto) : BaseResponse<String> {
        return BaseResponse(data = userService.saveUserIfNot(userDto, true))
    }

    @PostMapping("/switch/push")
    fun switchPushNotificationEnable(@RequestBody pushNotificationSettingRequest: PushNotificationSettingRequest) {
        userService.switchNotificationSetting(pushNotificationSettingRequest)
    }

    @GetMapping("/info/{uuId}")
    fun userInfo(@PathVariable(name = "uuId") uuId: String) : BaseResponse<UserDataDto> {
        return BaseResponse(data = userService.getUserInfo(uuId))
    }

}