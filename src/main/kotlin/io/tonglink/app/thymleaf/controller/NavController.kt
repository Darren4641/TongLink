package io.tonglink.app.thymleaf.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class NavController {

    @GetMapping
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
        model.addAttribute("nav", "mypage")
        return "mypage"
    }
}