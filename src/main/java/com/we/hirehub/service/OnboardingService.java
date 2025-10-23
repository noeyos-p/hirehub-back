package com.we.hirehub.service;

import com.we.hirehub.dto.OnboardingForm;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UsersRepository usersRepository;

    @Transactional
    public void save(String email, OnboardingForm form) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        if (form.getDisplayName() != null && !form.getDisplayName().isBlank()) {
            user.setName(form.getDisplayName());
        }
        if (form.getPhone() != null && !form.getPhone().isBlank()) {
            user.setPhone(form.getPhone());
        }

        // TODO: educations/careers/resume 매핑이 실제 엔티티에 필요하면 여기에 추가

        usersRepository.save(user); // 더티체킹으로도 되지만 명시 호출 OK
    }
}
