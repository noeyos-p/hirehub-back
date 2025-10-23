package com.we.hirehub.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostsDto {
    private Long id;
    private String title;
    private String content;
    private LocalDate startAt;
    private LocalDate endAt;
    private String location;
    private String careerLevel;
    private String education;
    private String type;
    private String salary;
    private String companyName;
}
