package io.tonglink.app.thymleaf.controller

import io.tonglink.app.common.security.UserPrincipal
import io.tonglink.app.common.security.service.OAuth2UserService
import io.tonglink.app.common.util.VapidUtil
import io.tonglink.app.user.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class NavController (
    val userService: UserService
) {

    @GetMapping
    fun index(model: Model) : String {
        return "index"
    }

    @GetMapping("/home")
    fun home(model: Model) : String {
        model.addAttribute("nav", "home")
        return "home"
    }

    @GetMapping("/rank")
    fun rank(model: Model) : String {
        model.addAttribute("nav", "rank")
        return "rank"
    }

    @GetMapping("/mypage")
    fun myPage(model: Model) : String {
        val authentication = SecurityContextHolder.getContext().authentication

        model.addAttribute("nav", "mypage")

        // 로그인 여부 판별
        if (authentication is OAuth2AuthenticationToken) {
            // 로그인한 사용자일 경우
            val principal = authentication.principal as UserPrincipal
            val loginUser = userService.getUserInfo(principal.uuId!!)
            model.addAttribute("UUID", loginUser.uuId)
            model.addAttribute("isPushEnabled", loginUser.isPushEnabled)
            model.addAttribute("email", loginUser.email)
            return "mypage"
        }

        // 비로그인 사용자 (anonymousUser)
        model.addAttribute("UUID", null)
        model.addAttribute("isPushEnabled", false)

        return "mypage"
    }

    @GetMapping("/oauth2")
    fun oauth2(model: Model,
               @RequestParam("uuId") uuId: String,
               @RequestParam("token") token: String) : String {

        val loginUser = userService.getOauth2UserInfo(uuId, token)
        model.addAttribute("uuId", uuId)
        model.addAttribute("token", token)

        return "oauth2"
    }

    @GetMapping("/terms-of-service")
    fun terms(model: Model) : String {
        return "terms-of-service"
    }

    @GetMapping("/privacy-policy")
    fun privacy(model: Model) : String {
        return "privacy-policy"
    }
}