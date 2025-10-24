package com.we.hirehub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ✅ JWT 인증 필터 (정식 수정 버전)
 * - Principal로 userId(Long)을 직접 주입
 * - 이후 Controller 단에서 auth.getPrincipal() → Long 정상 인식됨
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService; // dbUserDetailsService 명시적 주입

    public JwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            @Qualifier("dbUserDetailsService") UserDetailsService userDetailsService
    ) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && tokenProvider.validate(token)) {

            // ✅ 토큰에서 userId 추출 (Long)
            Long userId = tokenProvider.getUserId(token);

            // ✅ 필요하다면 username도 가져오기
            String username = tokenProvider.getUsername(token);

            // DB 조회는 optional (권한용)
            UserDetails userDetails = null;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (Exception ignored) {
            }

            // ✅ Principal을 userId(Long)으로 설정
            var auth = new UsernamePasswordAuthenticationToken(
                    userId, // ← 핵심: principal에 userId 넣기
                    null,
                    userDetails != null ? userDetails.getAuthorities() : null
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
