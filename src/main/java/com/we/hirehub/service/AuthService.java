package com.we.hirehub.service;

import com.we.hirehub.dto.SignupEmailRequest;
import com.we.hirehub.dto.SignupRequest;   // 기존(풀 온보딩용) DTO
import com.we.hirehub.entity.Role;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    /** 신규: 최소 정보로 가입 (이메일/비번) */
    @Transactional
    public void signupMinimal(SignupEmailRequest req) {
        if (usersRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("EMAIL_ALREADY_EXISTS");
        }

        Users u = new Users();
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.USER);

        // ★ NOT NULL 컬럼 대비 기본값들
        if (u.getName() == null) u.setName("");
        if (u.getNickname() == null) u.setNickname("");
        if (u.getPhone() == null) u.setPhone("");

        // dob / gender 기본값 추가
        if (u.getDob() == null || u.getDob().isBlank()) {
            u.setDob("1970-01-01"); // ← 문자열로
        }
        if (u.getGender() == null || u.getGender().isBlank()) {
            u.setGender("UNKNOWN"); // enum이면 실제 상수로 바꿔줘: Gender.UNKNOWN
        }
        // gender가 String이면:
        // if (u.getGender() == null) u.setGender("UNKNOWN");
        // gender가 enum이면:
        // if (u.getGender() == null) u.setGender(Gender.UNKNOWN);

        usersRepository.save(u);
    }

    /** 호환용: 예전 컨트롤러가 부르는 메서드 (이메일/비번만 써서 최소가입으로 위임) */
    @Transactional
    public void signup(SignupRequest full) {   // ← 이 메서드 추가
        // 풀 DTO에 이메일/비번 필드명이 다르면 아래 getter 이름만 맞춰줘
        SignupEmailRequest req = new SignupEmailRequest(full.getEmail(), full.getPassword());
        signupMinimal(req);
        // 필요하면 full의 나머지 필드는 온보딩 단계에서 업데이트
    }
}
