package com.we.hirehub.dto;

import lombok.Data;

/**
 * 프로필 수정 요청 DTO
 * - 이메일 제외(수정 불가)
 * - 들어온 값(null 아님)만 부분 업데이트
 * - 필드 이름은 Users 엔티티의 프로퍼티와 가능한 한 동일하게 사용
 */
@Data
public class MyProfileUpdateRequest {
    private String nickname;
    private String name;
    private String phone;          // phoneNumber / tel 등과 매핑
    private String birth;          // "yyyy-MM-dd" 형식 문자열(or LocalDate로 바꿔도 됨)
    private Integer age;
    private String gender;
    private String address;
    private String region;
    private String job;            // 직무
    private String career;         // 경력(연차/텍스트)
    private String education;      // 학력
    private String intro;          // 한줄소개/자기소개
    // 필요하면 프론트 항목명을 여기 더 추가 (email은 절대 넣지 말 것)
}
