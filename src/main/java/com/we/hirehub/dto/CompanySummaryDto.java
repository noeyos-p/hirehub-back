package com.we.hirehub.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompanySummaryDto {
    private Long id;
    private String name;
    private String logoUrl;
    private String location;
}
