package com.we.hirehub.service;

import com.we.hirehub.dto.*;
import com.we.hirehub.entity.Resume;
import com.we.hirehub.entity.Users;
import com.we.hirehub.exception.ForbiddenEditException;
import com.we.hirehub.exception.ResourceNotFoundException;
import com.we.hirehub.repo.ResumeRepository;
import com.we.hirehub.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public PagedResponse<ResumeDto> list(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateAt"));
        Page<Resume> p = resumeRepository.findByUsers_Id(userId, pageable);

        return new PagedResponse<>(
                p.getContent().stream().map(this::toDto).collect(Collectors.toList()),
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()
        );
    }

    public ResumeDto get(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUsers_Id(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("이력서를 찾을 수 없습니다."));
        return toDto(resume);
    }

    @Transactional
    public ResumeDto create(Long userId, ResumeUpsertRequest req) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));

        Resume resume = Resume.builder()
                .title(req.title())
                .idPhoto(req.idPhoto())
                .essayTittle(req.essayTitle())
                .essayContent(req.essayContent())
                .createAt(LocalDate.now())
                .updateAt(LocalDate.now())
                .locked(false)
                .users(user)
                .build();

        Resume saved = resumeRepository.save(resume);
        return toDto(saved);
    }

    @Transactional
    public ResumeDto update(Long userId, Long resumeId, ResumeUpsertRequest req) {
        Resume resume = resumeRepository.findByIdAndUsers_Id(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("이력서를 찾을 수 없습니다."));

        // 제출(잠금)된 이력서 수정 금지
        if (resume.isLocked() || resumeRepository.existsByIdAndUsers_IdAndLockedTrue(resumeId, userId)) {
            throw new ForbiddenEditException("이미 제출된 이력서는 수정할 수 없습니다.");
        }

        resume.setTitle(req.title());
        resume.setIdPhoto(req.idPhoto());
        resume.setEssayTittle(req.essayTitle());
        resume.setEssayContent(req.essayContent());
        resume.setUpdateAt(LocalDate.now());

        Resume updated = resumeRepository.save(resume);
        return toDto(updated);
    }

    /** 🔥 삭제 (CRUD의 D) */
    @Transactional
    public void delete(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUsers_Id(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("이력서를 찾을 수 없습니다."));

        // 제출(잠금)된 이력서 삭제 금지 (정책 동일 적용)
        if (resume.isLocked() || resumeRepository.existsByIdAndUsers_IdAndLockedTrue(resumeId, userId)) {
            throw new ForbiddenEditException("이미 제출된 이력서는 삭제할 수 없습니다.");
        }

        resumeRepository.delete(resume);
    }

    private ResumeDto toDto(Resume r) {
        return new ResumeDto(
                r.getId(),
                r.getTitle(),
                r.getIdPhoto(),
                r.getEssayTittle(),
                r.getEssayContent(),
                r.isLocked(),
                r.getCreateAt(),
                r.getUpdateAt()
        );
    }

    /** 내 프로필 조회 */
    public MyProfileDto getProfile(Long userId) {
        Users u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));

        // MyProfileDto가 롬복 @Data 형태라고 가정 (세터 존재)
        MyProfileDto dto = new MyProfileDto();
        BeanUtils.copyProperties(u, dto); // Users → DTO 동일명 필드 일괄 복사(없으면 무시)
        return dto;
    }

    /** 내 프로필 수정(이메일 제외, null 아닌 필드만 부분 업데이트) */
    @Transactional
    public MyProfileDto updateProfile(Long userId, MyProfileUpdateRequest req) {
        Users u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 부분 업데이트: req의 null이 아닌 필드만 Users에 반영
        BeanWrapper target = new BeanWrapperImpl(u);
        BeanWrapper source = new BeanWrapperImpl(req);

        for (var pd : source.getPropertyDescriptors()) {
            String name = pd.getName();
            if ("class".equals(name)) continue;
            if ("email".equalsIgnoreCase(name)) continue;          // 이메일은 무조건 제외
            if (!target.isWritableProperty(name)) continue;        // Users에 동일 필드 없으면 skip

            Object val = source.getPropertyValue(name);
            if (val != null) {
                try {
                    target.setPropertyValue(name, val);
                } catch (Exception ignore) {
                    // 타입 안 맞거나 변환 불가 시 안전하게 skip (필요하면 매핑표에 추가해 수동 변환)
                }
            }
        }

        // 업데이트 시각 필드가 있으면 갱신
        if (target.isWritableProperty("updateAt")) {
            target.setPropertyValue("updateAt", java.time.LocalDate.now());
        } else if (target.isWritableProperty("updatedAt")) {
            target.setPropertyValue("updatedAt", java.time.LocalDateTime.now());
        }

        Users saved = userRepository.save(u);

        MyProfileDto dto = new MyProfileDto();
        BeanUtils.copyProperties(saved, dto);
        return dto;
    }
}
