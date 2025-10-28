package com.we.hirehub.dto;

import java.time.LocalDate;

/**
 * ✅ ResumeDto
 * - 이력서(Resume) DTO + 사용자 온보딩 프로필(UserProfileMiniDto) 포함
 * - 기존 코드 호환을 위해 profile이 없는 생성자도 함께 제공
 */
public record ResumeDto(
        Long id,
        String title,
        String idPhoto,
        String essayTitle,
        String essayContent,
        String htmlContent,
        boolean locked,
        LocalDate createAt,
        LocalDate updateAt,
        UserProfileMiniDto profile // 온보딩 정보
) {

    /** ✅ 기존 코드 호환용 생성자 (profile 생략 가능) */
    public ResumeDto(
            Long id,
            String title,
            String idPhoto,
            String essayTitle,
            String essayContent,
            String htmlContent,
            boolean locked,
            LocalDate createAt,
            LocalDate updateAt
    ) {
        this(id, title, idPhoto, essayTitle, essayContent, htmlContent, locked, createAt, updateAt, null);
    }
}
