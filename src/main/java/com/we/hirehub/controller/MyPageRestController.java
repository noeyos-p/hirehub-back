package com.we.hirehub.controller;

import com.we.hirehub.dto.*;
import com.we.hirehub.service.MyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage") // 원 경로 고정
public class MyPageRestController {

    private final MyPageService myPageService;

    /** JWT 기반: SecurityContext의 Principal에서 userId 추출 */
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

    // ====== 이력서 CRUD ======

    // 목록: GET /api/mypage/resumes?page=0&size=10
    @GetMapping("/resumes")
    public PagedResponse<ResumeDto> list(Authentication auth,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return myPageService.list(userId(auth), page, size);
    }

    // 단건: GET /api/mypage/resumes/{resumeId}
    @GetMapping("/resumes/{resumeId}")
    public ResumeDto get(Authentication auth, @PathVariable Long resumeId) {
        return myPageService.get(userId(auth), resumeId);
    }

    // 생성: POST /api/mypage/resumes
    @PostMapping("/resumes")
    public ResumeDto create(Authentication auth, @Valid @RequestBody ResumeUpsertRequest req) {
        return myPageService.create(userId(auth), req);
    }

    // 수정: PUT /api/mypage/resumes/{resumeId}
    @PutMapping("/resumes/{resumeId}")
    public ResumeDto update(Authentication auth,
                            @PathVariable Long resumeId,
                            @Valid @RequestBody ResumeUpsertRequest req) {
        return myPageService.update(userId(auth), resumeId, req);
    }

    // 삭제: DELETE /api/mypage/resumes/{resumeId}
    @DeleteMapping("/resumes/{resumeId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long resumeId) {
        myPageService.delete(userId(auth), resumeId);
        return ResponseEntity.noContent().build(); // 204
    }

    // ====== 내 프로필 조회/수정 (이메일 제외 수정 가능) ======

    /** 내 프로필 조회 */
    @GetMapping("/me")
    public ResponseEntity<MyProfileDto> getMe(Authentication auth) {
        return ResponseEntity.ok(myPageService.getProfile(userId(auth)));
    }

    /** 내 프로필 수정 (이메일 제외, null 아닌 필드만 부분 업데이트) */
    @PutMapping("/me")
    public ResponseEntity<MyProfileDto> updateMe(Authentication auth,
                                                 @RequestBody MyProfileUpdateRequest req) {
        return ResponseEntity.ok(myPageService.updateProfile(userId(auth), req));
    }
}
