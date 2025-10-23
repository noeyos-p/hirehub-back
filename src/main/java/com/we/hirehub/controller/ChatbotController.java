package com.we.hirehub.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatbotController {

    /** 우하단 버튼이 띄우는 별도 챗봇 페이지 */
    @GetMapping("/chatbot")
    public String chatbot(Authentication auth, Model model) {
        boolean isLogin = (auth != null);
        model.addAttribute("isLogin", isLogin);
        model.addAttribute("username", isLogin ? auth.getName() : null);
        return "chatbot";
    }
}
