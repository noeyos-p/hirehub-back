package com.we.hirehub.service;

import com.we.hirehub.dto.*;
import com.we.hirehub.entity.Resume;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UsersRepository usersRepository;
    private final UserRepository userRepository; // 기존 프로젝트 호환 (Users와 같은 테이블)
    private final ResumeRepository resumeRepository;
    private final ApplyRepository applyRepository;

    public Users getUserOrThrow(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    public Optional<Users> findByEmail(String email) {
        try {
            return usersRepository.findByEmail(email);
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    // --- My Info ---
    public MyProfileDto getProfile(Long userId) {
        Users u = getUserOrThrow(userId);
        return MyProfileDto.builder()
                .id(u.getId())
                .email(u.getEmail())
                .nickname(u.getNickname())
                .phone(u.getPhone())
                .dob(u.getDob())
                .gender(u.getGender())
                .education(u.getEducation())
                .careerLevel(u.getCareerLevel())
                .position(u.getPosition())
                .address(u.getAddress())
                .location(u.getLocation())
                .build();
    }

    @Transactional
    public MyProfileDto updateProfile(Long userId, MyProfileDto req) {
        Users u = getUserOrThrow(userId);
        if (req.getNickname() != null) u.setNickname(req.getNickname());
        if (req.getPhone() != null) u.setPhone(req.getPhone());
        if (req.getDob() != null) u.setDob(req.getDob());
        if (req.getGender() != null) u.setGender(req.getGender());
        if (req.getEducation() != null) u.setEducation(req.getEducation());
        if (req.getCareerLevel() != null) u.setCareerLevel(req.getCareerLevel());
        if (req.getPosition() != null) u.setPosition(req.getPosition());
        if (req.getAddress() != null) u.setAddress(req.getAddress());
        if (req.getLocation() != null) u.setLocation(req.getLocation());
        usersRepository.save(u);
        return getProfile(userId);
    }

    // --- Resumes ---
    public List<ResumeDto> listResumes(Long userId) {
        return resumeRepository.findByUsers_Id(userId).stream()
                .map(r -> {
                    ResumeDto.ResumeDtoBuilder b = ResumeDto.builder()
                            .id(r.getId());
                    // usersId
                    if (r.getUsers() != null) {
                        b.usersId(r.getUsers().getId());
                    }
                    // title (엔티티에 title 필드가 존재한다는 전제 - 프로젝트 DTO가 title을 노출함)
                    b.title(r.getTitle());
                    return b.build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ResumeDto createResume(Long userId, ResumeDto req) {
        Users u = getUserOrThrow(userId);
        Resume r = new Resume();
        // 연관관계
        r.setUsers(u);
        // 제목만 세팅 (content/createdAt 등은 엔티티/DTO에 없음)
        r.setTitle(req.getTitle());
        Resume saved = resumeRepository.save(r);

        return ResumeDto.builder()
                .id(saved.getId())
                .usersId(u.getId())
                .title(saved.getTitle())
                .build();
    }

    public ResumeDto getResume(Long resumeId) {
        Resume r = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        ResumeDto.ResumeDtoBuilder b = ResumeDto.builder()
                .id(r.getId())
                .title(r.getTitle());
        if (r.getUsers() != null) {
            b.usersId(r.getUsers().getId());
        }
        return b.build();
    }

    @Transactional
    public ResumeDto updateResume(Long resumeId, ResumeDto req) {
        Resume r = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        if (req.getTitle() != null) {
            r.setTitle(req.getTitle());
        }
        Resume saved = resumeRepository.save(r);
        return getResume(saved.getId());
    }

    @Transactional
    public void deleteResume(Long resumeId) {
        resumeRepository.deleteById(resumeId);
    }

    public List<ApplicationSummaryDto> listApplications(Long userId) {
        // Apply는 user를 직접 갖지 않으므로 resume.users.id 경로로 조회
        return applyRepository.findByResume_Users_Id(userId).stream().map(a -> {
            Long jobPostId = null;
            String jobTitle = null;
            String companyName = null;

            if (a.getJobPosts() != null) {
                jobPostId = a.getJobPosts().getId();
                jobTitle = a.getJobPosts().getTitle();
                if (a.getJobPosts().getCompany() != null) {
                    companyName = a.getJobPosts().getCompany().getName(); // Company.name
                }
            }

            String appliedAt = (a.getApplyAt() != null) ? a.getApplyAt().toString() : null;
            String status = null; // Apply에 status 없음. 필요시 기본값 "APPLIED" 등 사용 가능.

            return ApplicationSummaryDto.builder()
                    .id(a.getId())
                    .jobPostId(jobPostId)
                    .jobTitle(jobTitle)
                    .companyName(companyName)
                    .appliedAt(appliedAt)
                    .status(status)
                    .build();
        }).toList();
    }
}