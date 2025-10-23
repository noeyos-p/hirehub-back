package com.we.hirehub.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** 메인 페이지 */
    @GetMapping("/")
    public String index(Authentication auth, Model model) {
        if (auth != null) {
            model.addAttribute("isLogin", true);
            model.addAttribute("username", auth.getName());
        } else {
            model.addAttribute("isLogin", false);
            model.addAttribute("username", null);
        }
        return "index";
    }

    /** 로그인 페이지 (스프링 시큐리티 기본 로그인 대신 템플릿 사용 시) */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ✅ 여기엔 /chatbot 매핑 두지 마세요(중복 방지). ChatbotController에서만 처리.
}
