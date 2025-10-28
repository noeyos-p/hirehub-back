package com.we.hirehub.controller.admin;

import com.we.hirehub.dto.ResumeDto;
import com.we.hirehub.entity.Resume;
import com.we.hirehub.service.S3Service;
import com.we.hirehub.service.admin.ResumeAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ✅ 관리자 - 이력서 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/resume-management")
@RequiredArgsConstructor
public class ResumeAdminController {

    private final ResumeAdminService resumeService;
    private final S3Service s3Service;

    /** ✅ [1] 이력서 목록 조회 */
    @GetMapping
    public ResponseEntity<?> getAllResumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createAt,desc") String sort) {

        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc"))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            Page<Resume> resumes = resumeService.getAllResumes(pageable);

            List<ResumeDto> resumeDtos = resumes.getContent().stream()
                    .map(resumeService::toDto)
                    .collect(Collectors.toList());

            log.info("📄 관리자 이력서 목록 조회 완료: {}개", resumes.getTotalElements());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalElements", resumes.getTotalElements(),
                    "totalPages", resumes.getTotalPages(),
                    "size", resumes.getSize(),
                    "number", resumes.getNumber(),
                    "content", resumeDtos
            ));

        } catch (Exception e) {
            log.error("❌ 관리자 이력서 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(createErrorResponse(e.getMessage()));
        }
    }

    /** ✅ [2] 이력서 상세 조회 */
    @GetMapping("/{resumeId}")
    public ResponseEntity<?> getResumeById(@PathVariable Long resumeId) {
        try {
            Resume resume = resumeService.getResumeById(resumeId);
            ResumeDto resumeDto = resumeService.toDto(resume);

            log.info("📄 관리자 이력서 상세 조회 성공 - id: {}", resumeId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", resumeDto
            ));

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 이력서 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ 이력서 상세 조회 중 오류", e);
            return ResponseEntity.internalServerError().body(createErrorResponse(e.getMessage()));
        }
    }

    /** ✅ [3] 이력서 생성 */
    @PostMapping
    public ResponseEntity<?> createResume(@RequestBody Resume resume) {
        try {
            if (resume.getTitle() == null || resume.getTitle().isBlank()) {
                return ResponseEntity.badRequest().body(createErrorResponse("이력서 제목이 필요합니다"));
            }

            Resume created = resumeService.createResume(resume);
            ResumeDto dto = resumeService.toDto(created);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "이력서 생성 성공",
                    "data", dto
            ));

        } catch (Exception e) {
            log.error("❌ 이력서 생성 실패", e);
            return ResponseEntity.internalServerError().body(createErrorResponse(e.getMessage()));
        }
    }

    /** ✅ [4] 이력서 수정 */
    @PutMapping("/{resumeId}")
    public ResponseEntity<?> updateResume(
            @PathVariable Long resumeId,
            @RequestBody Map<String, Object> updateData) {

        try {
            if (resumeId == null || resumeId <= 0) {
                return ResponseEntity.badRequest().body(createErrorResponse("유효한 이력서 ID가 필요합니다"));
            }

            Resume updated = resumeService.updateResume(resumeId, updateData);
            ResumeDto dto = resumeService.toDto(updated);

            log.info("📝 이력서 수정 완료 - id: {}", resumeId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이력서 수정 완료",
                    "data", dto
            ));

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 이력서 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ 이력서 수정 중 오류", e);
            return ResponseEntity.internalServerError().body(createErrorResponse(e.getMessage()));
        }
    }

    /** ✅ [5] 이력서 삭제 */
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<?> deleteResume(@PathVariable Long resumeId) {
        try {
            if (resumeId == null || resumeId <= 0) {
                return ResponseEntity.badRequest().body(createErrorResponse("유효한 이력서 ID가 필요합니다"));
            }

            Resume resume = resumeService.getResumeById(resumeId);

            // S3 증명사진 삭제
            if (resume.getIdPhoto() != null && !resume.getIdPhoto().isEmpty()) {
                try {
                    s3Service.deleteFile(resume.getIdPhoto());
                    log.info("🗑️ S3 증명사진 삭제 완료: {}", resume.getIdPhoto());
                } catch (Exception ex) {
                    log.error("⚠️ S3 증명사진 삭제 실패: {}", resume.getIdPhoto(), ex);
                }
            }

            resumeService.deleteResume(resumeId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이력서 삭제 완료",
                    "deletedResumeId", resumeId
            ));

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 이력서 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ 이력서 삭제 중 오류", e);
            return ResponseEntity.internalServerError().body(createErrorResponse(e.getMessage()));
        }
    }

    /** ✅ [6] 이력서 잠금 */
    @PostMapping("/{resumeId}/lock")
    public ResponseEntity<?> lockResume(@PathVariable Long resumeId) {
        try {
            Resume locked = resumeService.lockResume(resumeId);
            ResumeDto dto = resumeService.toDto(locked);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이력서 잠금 완료 (지원 완료)",
                    "data", dto
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ 이력서 잠금 중 오류", e);
            return ResponseEntity.internalServerError().body(createErrorResponse(e.getMessage()));
        }
    }

    /** ✅ [7] 이력서 잠금 해제 */
    @PostMapping("/{resumeId}/unlock")
    public ResponseEntity<?> unlockResume(@PathVariable Long resumeId) {
        try {
            Resume unlocked = resumeService.unlockResume(resumeId);
            ResumeDto dto = resumeService.toDto(unlocked);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이력서 잠금 해제 완료",
                    "data", dto
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ 이력서 잠금 해제 중 오류", e);
            return ResponseEntity.internalServerError().body(createErrorResponse(e.getMessage()));
        }
    }

    /** ✅ [8] 증명사진 업로드 */
    @PostMapping("/resume-photo")
    public ResponseEntity<?> uploadResumePhoto(
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("📤 Uploading resume photo - userId: {}, fileName: {}", userId, file.getOriginalFilename());

            String fileUrl = s3Service.uploadResumePhoto(file, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이력서 증명사진 업로드 성공",
                    "fileUrl", fileUrl,
                    "userId", userId,
                    "fileName", file.getOriginalFilename(),
                    "fileSize", file.getSize()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ 증명사진 업로드 실패", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("업로드 실패: " + e.getMessage()));
        }
    }

    /** 공통 에러 응답 */
    private Map<String, Object> createErrorResponse(String message) {
        return Map.of(
                "success", false,
                "message", message
        );
    }
}
