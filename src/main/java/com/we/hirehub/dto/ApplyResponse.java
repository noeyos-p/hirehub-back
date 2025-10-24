package com.we.hirehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ApplyResponse {
    private Long id;
    private String companyName;
    private String resumeTitle;
    private LocalDate appliedAt;
}
