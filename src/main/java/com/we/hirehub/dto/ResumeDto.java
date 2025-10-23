package com.we.hirehub.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResumeDto {
    private Long id;
    private Long usersId;
    private String title;
}
