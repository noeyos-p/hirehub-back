package com.we.hirehub.controller;

import com.we.hirehub.dto.*;
import com.we.hirehub.service.MyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageRestController {

    private final MyPageService myPageService;

    private Long userId(Authentication auth) {
        if (auth == null) {
            auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) throw new IllegalStateException("인증 정보가 없습니다.");
        }
        Object p = auth.getPrincipal();
        if (p instanceof Long l) return l;
        if (p instanceof String s) { try { return Long.parseLong(s); } catch (NumberFormatException ignore) {} }
        try {
            var m = p.getClass().getMethod("getId");
            Object v = m.invoke(p);
            if (v instanceof Long l) return l;
            if (v instanceof String s) return Long.parseLong(s);
        } catch (Exception ignore) {}
        throw new IllegalStateException("현재 사용자 ID를 확인할 수 없습니다.");
    }

    // ====== 이력서 CRUD (기존 유지) ======

    @GetMapping("/resumes")
    public PagedResponse<ResumeDto> list(Authentication auth,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return myPageService.list(userId(auth), page, size);
    }

    @GetMapping("/resumes/{resumeId}")
    public ResumeDto get(Authentication auth, @PathVariable Long resumeId) {
        return myPageService.get(userId(auth), resumeId);
    }

    @PostMapping("/resumes")
    public ResumeDto create(Authentication auth, @Valid @RequestBody ResumeUpsertRequest req) {
        return myPageService.create(userId(auth), req);
    }

    @PutMapping("/resumes/{resumeId}")
    public ResumeDto update(Authentication auth,
                            @PathVariable Long resumeId,
                            @Valid @RequestBody ResumeUpsertRequest req) {
        return myPageService.update(userId(auth), resumeId, req);
    }

    @DeleteMapping("/resumes/{resumeId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long resumeId) {
        myPageService.delete(userId(auth), resumeId);
        return ResponseEntity.noContent().build();
    }

    // ====== 내 프로필 (기존 유지) ======

    @GetMapping("/me")
    public ResponseEntity<MyProfileDto> getMe(Authentication auth) {
        return ResponseEntity.ok(myPageService.getProfile(userId(auth)));
    }

    @PutMapping("/me")
    public ResponseEntity<MyProfileDto> updateMe(Authentication auth,
                                                 @RequestBody MyProfileUpdateRequest req) {
        return ResponseEntity.ok(myPageService.updateProfile(userId(auth), req));
    }

    // ====== 지원내역 (기존 유지) ======

    @GetMapping("/applies")
    public ResponseEntity<List<ApplyResponse>> getMyApplies(Authentication auth) {
        return ResponseEntity.ok(myPageService.getMyApplyList(userId(auth)));
    }

    // ====== ⭐ 신규: 관심 기업 ======

    /** 관심 기업 목록 (회사명 + 공고수) */
    @GetMapping("/favorites/companies")
    public PagedResponse<FavoriteCompanySummaryDto> favoriteCompanies(Authentication auth,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size) {
        return myPageService.listFavoriteCompanies(userId(auth), page, size);
    }

//    /** 관심 기업 추가 (companyId 기반) */
//    @PostMapping("/favorites/companies")
//    public FavoriteCompanySummaryDto addFavoriteCompany(Authentication auth,
//                                                        @Valid @RequestBody FavoriteCompanyAddRequest req) {
//        return myPageService.addFavoriteCompany(userId(auth), req.getCompanyId());
//    }

    /** 관심 기업 삭제 (companyId 기반) */
    @DeleteMapping("/favorites/companies/{companyId}")
    public ResponseEntity<Void> removeFavoriteCompany(Authentication auth,
                                                      @PathVariable Long companyId) {
        myPageService.removeFavoriteCompany(userId(auth), companyId);
        return ResponseEntity.noContent().build();
    }
}
