package com.we.hirehub.controller;

import com.we.hirehub.config.JwtTokenProvider;
import com.we.hirehub.dto.LoginRequest;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UsersRepository usersRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        // 이메일로 Users 조회 → userId 확보
        Users u = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        String accessToken = tokenProvider.createToken(request.getEmail(), u.getId()); // ★ uid 포함

        return ResponseEntity.ok(Map.of(
                "tokenType", "Bearer",
                "accessToken", accessToken
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(java.security.Principal principal) {
        return (principal == null)
                ? ResponseEntity.status(401).body(Map.of("error","UNAUTHORIZED","message","인증이 필요합니다."))
                : ResponseEntity.ok(Map.of("name", principal.getName()));
    }
}
