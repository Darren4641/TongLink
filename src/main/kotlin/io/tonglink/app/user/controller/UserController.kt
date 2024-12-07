package io.tonglink.app.user.controller

import com.example.kopring.common.response.BaseResponse
import io.tonglink.app.user.dto.UserDto
import io.tonglink.app.user.service.UserService
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

}