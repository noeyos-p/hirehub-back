package com.we.hirehub.controller;

import com.we.hirehub.dto.*;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repo.UsersRepository;
import com.we.hirehub.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;
    private final UsersRepository usersRepository;

    private Long resolveUserId(UserDetails principal){
        if (principal == null) {
            throw new IllegalArgumentException("Unauthenticated");
        }
        String email = principal.getUsername();
        Users u = usersRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found by email: " + email));
        return u.getId();
    }

    // --- My Info ---
    @GetMapping("/profile")
    public ResponseEntity<MyProfileDto> profile(@AuthenticationPrincipal UserDetails principal){
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(myPageService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<MyProfileDto> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody MyProfileDto req){
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(myPageService.updateProfile(userId, req));
    }

    // --- Resumes ---
    @GetMapping("/resumes")
    public ResponseEntity<List<ResumeDto>> listResumes(@AuthenticationPrincipal UserDetails principal){
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(myPageService.listResumes(userId));
    }

    @PostMapping("/resumes")
    public ResponseEntity<ResumeDto> createResume(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody ResumeDto req){
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(myPageService.createResume(userId, req));
    }

    @GetMapping("/resumes/{id}")
    public ResponseEntity<ResumeDto> getResume(@PathVariable Long id){
        return ResponseEntity.ok(myPageService.getResume(id));
    }

    @PutMapping("/resumes/{id}")
    public ResponseEntity<ResumeDto> updateResume(@PathVariable Long id, @RequestBody ResumeDto req){
        return ResponseEntity.ok(myPageService.updateResume(id, req));
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id){
        myPageService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }

    // --- Applications (지원 내역) ---
    @GetMapping("/applied")
    public ResponseEntity<List<ApplicationSummaryDto>> listApplications(
            @AuthenticationPrincipal UserDetails principal){
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(myPageService.listApplications(userId));
    }

    // --- Favorites / Calendar / My Posts (스텁: 빈 목록 반환) ---
    @GetMapping("/favorites/companies")
    public ResponseEntity<List<CompanySummaryDto>> favoriteCompanies(){
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/favorites/notices")
    public ResponseEntity<List<JobPostSummaryDto>> favoriteNotices(){
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarEventDto>> calendar(){
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostSummaryDto>> myPosts(){
        return ResponseEntity.ok(List.of());
    }
}
