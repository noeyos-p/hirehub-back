package com.we.hirehub.controller;

import com.we.hirehub.dto.OnboardingForm;
import com.we.hirehub.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingRestController {

    private final OnboardingService onboardingService;

    /**
     * 로그인 사용자 기준으로 온보딩 정보를 저장한다.
     * - principal.getUsername() == 로그인 아이디(이메일) 기준
     * - 엔티티 구조는 변경하지 않음
     */
    @PostMapping(
            value = "/save",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> save(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody OnboardingForm form
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "UNAUTHORIZED"));
        }

        final String email = principal.getUsername(); // 로그인 사용자의 이메일
        onboardingService.save(email, form);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
