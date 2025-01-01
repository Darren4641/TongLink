package io.tonglink.app.thymleaf.controller

import io.tonglink.app.common.security.UserPrincipal
import io.tonglink.app.common.security.service.OAuth2UserService
import io.tonglink.app.common.util.VapidUtil
import io.tonglink.app.user.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class NavController (
    val userService: UserService
) {

    @GetMapping
    fun home(model: Model) : String {
        model.addAttribute("nav", "home")
        //VapidUtil.generateVapidKeyPair()
        return "home"
    }

    @GetMapping("/rank")
    fun rank(model: Model) : String {
        model.addAttribute("nav", "rank")
        return "rank"
    }

    @GetMapping("/mypage")
    fun myPage(model: Model, @RequestParam("uuId") uuId: String) : String {
//        val authentication = SecurityContextHolder.getContext().authentication
//        val principal = authentication?.principal

        val loginUser = userService.getUserInfo(uuId)

        // 로그인 여부 판별
        if (loginUser.email != null) {
            // 로그인한 사용자일 경우
            println("@@ LoggedIn UserUUID : ${loginUser.email}")
            model.addAttribute("UUID", loginUser.uuId)
            model.addAttribute("isOauth", true)
            model.addAttribute("email", loginUser.email)
        } else {
            // 비로그인 사용자 (anonymousUser)
            println("@@ Anonymous User")
            model.addAttribute("UUID", null)
            model.addAttribute("isOauth", false)
        }

        model.addAttribute("nav", "mypage")
        return "mypage"
    }
}