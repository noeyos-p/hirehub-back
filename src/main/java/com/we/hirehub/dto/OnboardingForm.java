package com.we.hirehub.dto;

import lombok.Data;
import java.util.List;

@Data
public class OnboardingForm {
    private String displayName;
    private String phone;

    private List<EducationDto> educations;
    private List<CareerDto> careers;

    private String resumeUrl;
    private String resumeText;

    @Data
    public static class EducationDto {
        private String school;
        private String major;
        private String degree;
        private String period;
    }

    @Data
    public static class CareerDto {
        private String company;
        private String title;
        private String period;
        private String summary;
    }
}
