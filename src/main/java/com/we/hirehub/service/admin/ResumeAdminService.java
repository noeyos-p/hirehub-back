package com.we.hirehub.service.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.we.hirehub.dto.ResumeDto;
import com.we.hirehub.entity.Resume;
import com.we.hirehub.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeAdminService {

    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ JSON 파싱용

    // ============ 조회 ============

    public Page<Resume> getAllResumes(Pageable pageable) {
        log.debug("모든 이력서 조회");
        return resumeRepository.findAll(pageable);
    }

    public Resume getResumeById(Long resumeId) {
        log.debug("이력서 조회: {}", resumeId);
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + resumeId));
    }

    // ============ 생성 ============

    @Transactional
    public Resume createResume(Resume resume) {
        log.info("이력서 생성: {}", resume.getTitle());

        if (resume.getCreateAt() == null) {
            resume.setCreateAt(LocalDate.now());
        }
        if (resume.getUpdateAt() == null) {
            resume.setUpdateAt(LocalDate.now());
        }

        return resumeRepository.save(resume);
    }

    // ============ 수정 ============

    @Transactional
    public Resume updateResume(Long resumeId, Map<String, Object> updateData) {
        log.info("이력서 수정: {}", resumeId);
        Resume resume = getResumeById(resumeId);

        if (updateData.containsKey("title")) {
            resume.setTitle((String) updateData.get("title"));
        }
        if (updateData.containsKey("idPhoto")) {
            resume.setIdPhoto((String) updateData.get("idPhoto"));
        }
        if (updateData.containsKey("essayTittle")) {
            resume.setEssayTittle((String) updateData.get("essayTittle"));
        }
        if (updateData.containsKey("essayContent")) {
            resume.setEssayContent((String) updateData.get("essayContent"));
        }
        if (updateData.containsKey("locked")) {
            resume.setLocked((Boolean) updateData.get("locked"));
        }

        // ✅ 학력, 경력, 자격증, 스킬, 언어를 htmlContent로 변환
        try {
            Map<String, Object> htmlData = new HashMap<>();

            if (updateData.containsKey("educations")) {
                htmlData.put("education", updateData.get("educations"));
            }
            if (updateData.containsKey("careers")) {
                htmlData.put("career", updateData.get("careers"));
            }
            if (updateData.containsKey("certifications")) {
                htmlData.put("certificate", updateData.get("certifications"));
            }
            if (updateData.containsKey("skills")) {
                htmlData.put("skill", updateData.get("skills"));
            }
            if (updateData.containsKey("languages")) {
                htmlData.put("language", updateData.get("languages"));
            }

            // 기존 htmlContent와 병합
            if (resume.getHtmlContent() != null && !resume.getHtmlContent().isBlank()) {
                JsonNode existing = objectMapper.readTree(resume.getHtmlContent());
                Map<String, Object> existingData = objectMapper.convertValue(existing, Map.class);
                existingData.putAll(htmlData);
                resume.setHtmlContent(objectMapper.writeValueAsString(existingData));
            } else if (!htmlData.isEmpty()) {
                resume.setHtmlContent(objectMapper.writeValueAsString(htmlData));
            }
        } catch (Exception e) {
            log.error("htmlContent 생성 실패: {}", e.getMessage());
            throw new RuntimeException("이력서 데이터 저장 실패", e);
        }

        resume.setUpdateAt(LocalDate.now());
        return resumeRepository.save(resume);
    }

    // ============ 삭제 ============

    @Transactional
    public void deleteResume(Long resumeId) {
        log.info("이력서 삭제: {}", resumeId);
        if (!resumeRepository.existsById(resumeId)) {
            throw new IllegalArgumentException("존재하지 않는 이력서입니다");
        }
        resumeRepository.deleteById(resumeId);
    }

    // ============ 이력서 상태 관리 ============

    @Transactional
    public Resume lockResume(Long resumeId) {
        log.info("이력서 잠금 (지원 완료): {}", resumeId);
        Resume resume = getResumeById(resumeId);
        resume.setLocked(true);
        resume.setUpdateAt(LocalDate.now());
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume unlockResume(Long resumeId) {
        log.info("이력서 잠금 해제: {}", resumeId);
        Resume resume = getResumeById(resumeId);
        resume.setLocked(false);
        resume.setUpdateAt(LocalDate.now());
        return resumeRepository.save(resume);
    }

    // ============ 통계 ============

    public Long getTotalResumesCount() {
        log.debug("전체 이력서 수 조회");
        return resumeRepository.count();
    }

    // ============ DTO 변환 (관리자용) ============

    /**
     * Entity -> DTO 변환 (관리자용)
     * 학력, 경력, 자격증, 스킬, 언어 등 htmlContent(JSON) 포함
     */
    public ResumeDto toDto(Resume r) {
        // 🧩 사용자 정보 (관리자용)
        ResumeDto.UserInfo userInfo = null;
        if (r.getUsers() != null) {
            userInfo = new ResumeDto.UserInfo(
                    r.getUsers().getId(),
                    r.getUsers().getNickname(),
                    r.getUsers().getEmail()
            );
        }

        // 🧩 htmlContent 파싱 (학력, 경력, 자격증, 스킬, 언어)
        List<Map<String, Object>> education = Collections.emptyList();
        List<Map<String, Object>> career = Collections.emptyList();
        List<Map<String, Object>> certificate = Collections.emptyList();
        List<Map<String, Object>> skill = Collections.emptyList();
        List<Map<String, Object>> language = Collections.emptyList();

        try {
            if (r.getHtmlContent() != null && !r.getHtmlContent().isBlank()) {
                JsonNode root = objectMapper.readTree(r.getHtmlContent());
                education = extractList(root, "education");
                career = extractList(root, "career");
                certificate = extractList(root, "certificate");
                skill = extractList(root, "skill");
                language = extractList(root, "language");
            }
        } catch (Exception e) {
            log.warn("이력서 htmlContent 파싱 실패: {}", e.getMessage());
        }

        // ✅ 완성된 DTO를 한 번에 생성 (setter 사용 안 함)
        return new ResumeDto(
                r.getId(),
                r.getTitle(),
                r.getIdPhoto(),
                r.getEssayTittle(),
                r.getEssayContent(),
                r.getHtmlContent(),
                r.isLocked(),
                r.getCreateAt(),
                r.getUpdateAt(),
                null,          // profile (관리자 페이지엔 불필요)
                userInfo,      // 작성자 정보
                education,     // 학력
                career,        // 경력
                certificate,   // 자격증
                skill,         // 스킬
                language       // 언어
        );
    }

    /**
     * JSON 배열을 안전하게 Map 리스트로 변환
     */
    private List<Map<String, Object>> extractList(JsonNode root, String field) {
        if (!root.has(field) || !root.get(field).isArray()) {
            return Collections.emptyList();
        }

        try {
            List<Map<String, Object>> result = new ArrayList<>();
            JsonNode arrayNode = root.get(field);

            for (JsonNode node : arrayNode) {
                Map<String, Object> map = objectMapper.convertValue(node, Map.class);
                result.add(map);
            }

            return result;
        } catch (Exception e) {
            log.warn("필드 변환 실패 ({}): {}", field, e.getMessage());
            return Collections.emptyList();
        }
    }

}
