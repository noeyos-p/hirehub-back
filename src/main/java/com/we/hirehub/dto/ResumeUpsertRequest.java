package com.we.hirehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResumeUpsertRequest(
        @NotBlank @Size(max = 255) String title,
        String idPhoto,
        @Size(max = 255) String essayTitle,
        @NotBlank String essayContent
) {}
