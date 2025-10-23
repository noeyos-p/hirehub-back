package com.we.hirehub.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
//
//    // 로그인 페이지
//    @GetMapping("/login")
//    public String loginPage(HttpSession session) {
//        // 이미 로그인 되어있으면 홈으로
//        if (session.getAttribute("username") != null) {
//            return "redirect:/home";
//        }
//        return "login";
//    }
//
//    // 로그인 처리
//    @PostMapping("/login")
//    public String doLogin(@RequestParam String username,
//                          @RequestParam String password,
//                          HttpSession session,
//                          Model model) {
//        // 간단한 예: username=password 라면 로그인 성공
//        if (username != null && username.equals(password)) {
//            session.setAttribute("username", username);
//            return "redirect:/home";
//        } else {
//            model.addAttribute("error", "아이디/비밀번호가 올바르지 않습니다.");
//            return "login";
//        }
//    }

    // 홈 페이지
//    @GetMapping("/")
//    public String homePage(HttpSession session, Model model) {
//        Object username = session.getAttribute("username");
//        if (username == null) {
//            return "redirect:/login";
//        }
//        model.addAttribute("username", username);
//        return "index";
//    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
