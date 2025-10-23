package com.we.hirehub.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginHandler implements
        org.springframework.security.web.authentication.AuthenticationSuccessHandler,
        org.springframework.security.web.authentication.AuthenticationFailureHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 엔티티/서비스 변경 없이 진행하기 위해 별도 merge 호출 없음
        if (authentication.getPrincipal() instanceof OAuth2User o) {
            // 필요하면 email/name 사용 가능하지만 호출만 하지 않음
            // String email = String.valueOf(o.getAttributes().getOrDefault("email", ""));
            // String name  = String.valueOf(o.getAttributes().getOrDefault("name", ""));
        }

        // /signup/google에서 심은 플래그가 있으면 온보딩으로 보냄
        var session = request.getSession(false);
        if (session != null) {
            Object flag = session.getAttribute("forceOnboarding");
            if (flag instanceof Boolean && (Boolean) flag) {
                session.removeAttribute("forceOnboarding");
                response.sendRedirect("/onboarding");
                return;
            }
        }

        // 기본 이동
        response.sendRedirect("/");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
            throws IOException, ServletException {
        response.sendRedirect("/login?error=" + exception.getMessage());
    }
}
