// src/main/java/com/we/hirehub/controller/SignupController.java
package com.we.hirehub.controller;

import com.we.hirehub.dto.SignupRequest;
import com.we.hirehub.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/signup")
public class SignupController {

    private final AuthService authService;

    /** 가입 방법 선택 (구글/직접) */
    @GetMapping
    public String choice() {
        return "signup-choice";
    }

    /** 직접 가입 폼 */
    @GetMapping("/direct")
    public String directForm(Model model) {
        model.addAttribute("form", new SignupRequest());
        return "signup-direct";
    }

    /** 직접 가입 처리 */
    @PostMapping("/direct")
    public String submitDirect(@Valid @ModelAttribute("form") SignupRequest form,
                               BindingResult bindingResult,
                               Model model,
                               HttpSession session) {               // ✅ 세션 주입
        if (bindingResult.hasErrors()) {
            return "signup-direct";
        }
        try {
            authService.signup(form);                              // 네 기존 로직 그대로
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "signup-direct";
        }

        // ✅ 직접가입도 온보딩으로
        if (form.getEmail() != null && !form.getEmail().isBlank()) {
            session.setAttribute("pendingOnboardingEmail", form.getEmail());
        }
        return "redirect:/onboarding";
    }

    /** 구글 가입 시작: 온보딩 강제 플래그 심고 리다이렉트 */
    @GetMapping("/google")
    public String signupWithGoogle(HttpSession session) {
        session.setAttribute("forceOnboarding", Boolean.TRUE);
        // 실제 리다이렉트는 시큐리티 설정의 registrationId 사용
        return "redirect:/oauth2/authorization/google";
    }
}
