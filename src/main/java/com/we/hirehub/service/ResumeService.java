package com.we.hirehub.service;

import com.we.hirehub.dto.ResumeDto;
import com.we.hirehub.entity.Resume;
import com.we.hirehub.entity.Users;
import com.we.hirehub.exception.ForbiddenEditException;
import com.we.hirehub.exception.ResourceNotFoundException;
import com.we.hirehub.repository.ResumeRepository;
import com.we.hirehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResumeDto create(ResumeDto dto) {
        Long userId = resolveCurrentUserId();
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));

        Resume resume = Resume.builder()
                .title(dto.title())
                .idPhoto(dto.idPhoto())
                .essayTittle(dto.essayTitle())
                .essayContent(dto.essayContent())
                .createAt(LocalDate.now())
                .updateAt(LocalDate.now())
                .locked(false)
                .users(user)
                .build();

        Resume saved = resumeRepository.save(resume);
        return toDto(saved);
    }

    public List<ResumeDto> findByUser(Long userId) {
        return resumeRepository.findByUsers_Id(userId, org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ResumeDto update(Long id, ResumeDto dto) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("이력서를 찾을 수 없습니다."));

        // locked만으로 판정
        if (resume.isLocked() || resumeRepository.existsByIdAndLockedTrue(id)) {
            throw new ForbiddenEditException("이미 제출된 이력서는 수정할 수 없습니다.");
        }

        resume.setTitle(dto.title());
        resume.setIdPhoto(dto.idPhoto());
        resume.setEssayTittle(dto.essayTitle());
        resume.setEssayContent(dto.essayContent());
        resume.setUpdateAt(LocalDate.now());

        Resume updated = resumeRepository.save(resume);
        return toDto(updated);
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

    private Long resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalStateException("인증 정보가 없습니다.");
        Object p = auth.getPrincipal();
        if (p instanceof Long l) return l;
        if (p instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignore) {}
        }
        try {
            var m = p.getClass().getMethod("getId");
            Object v = m.invoke(p);
            if (v instanceof Long l) return l;
            if (v instanceof String s) return Long.parseLong(s);
        } catch (Exception ignored) {}
        throw new IllegalStateException("현재 사용자 ID를 확인할 수 없습니다.");
    }
}
