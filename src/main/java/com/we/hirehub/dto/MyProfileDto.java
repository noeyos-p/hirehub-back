package com.we.hirehub.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MyProfileDto {
    private Long id;
    private String email;
    private String nickname;
    private String phone;
    private String dob;        // Users의 dob(String) 가정
    private String gender;
    private String education;
    private String careerLevel;
    private String position;
    private String address;
    private String location;
}
