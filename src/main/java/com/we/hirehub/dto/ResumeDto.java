package com.we.hirehub.dto;

import java.time.LocalDate;

public record ResumeDto(
        Long id,
        String title,
        String idPhoto,
        String essayTitle,
        String essayContent,
        String htmlContent,  // 추가
        boolean locked,
        LocalDate createAt,
        LocalDate updateAt
) {}
