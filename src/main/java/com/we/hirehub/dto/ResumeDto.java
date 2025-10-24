package com.we.hirehub.dto;

import java.time.LocalDate;

public record ResumeDto(
        Long id,
        String title,
        String idPhoto,
        String essayTitle,
        String essayContent,
        boolean locked,
        LocalDate createAt,
        LocalDate updateAt
) {}
