package com.we.hirehub.controller;

import com.we.hirehub.dto.LoginRequest;
import com.we.hirehub.dto.SignupEmailRequest;
import com.we.hirehub.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    // 회원가입: /api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupEmailRequest req) {
        authService.signupMinimal(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", "OK"));
    }

    // 로그인: /api/auth/login  (세션에 SecurityContext 저장하여 이후 요청 인증 유지)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req,
                                   HttpServletRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );

            // ✅ SecurityContext를 세션에 저장
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            // 필요시 사용자 정보도 내려줄 수 있음
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "BAD_CREDENTIALS"));
        }
    }

    // ===== 예외 핸들러들 (반드시 클래스 블록 내부에 있어야 함) =====

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleBind(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "INVALID_REQUEST"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "DUPLICATE_OR_CONSTRAINT_VIOLATION"));
    }
}
