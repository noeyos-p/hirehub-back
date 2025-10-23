// src/main/java/com/we/hirehub/bootstrap/AdminSeeder.java
package com.we.hirehub.auth;

import com.we.hirehub.entity.Role;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class AdminSeeder {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seed() {
        usersRepository.findByEmail("admin@admin").ifPresentOrElse(
                u -> {}, // 존재하면 패스
                () -> {
                    Users a = new Users();
                    a.setEmail("admin@admin");                 // 로그인 아이디로 사용
                    a.setPassword(passwordEncoder.encode("admin123"));
                    a.setRole(Role.ADMIN);
                    a.setName("");
                    a.setNickname("");
                    a.setPhone("");
                    a.setDob("1970-01-01");
                    a.setGender("UNKNOWN");
                    usersRepository.save(a);
                }
        );
    }
}
