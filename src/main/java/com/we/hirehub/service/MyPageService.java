package com.we.hirehub.service;

import com.we.hirehub.dto.*;
import com.we.hirehub.entity.Apply;
import com.we.hirehub.entity.FavoriteCompany;
import com.we.hirehub.entity.Resume;
import com.we.hirehub.entity.Users;
import com.we.hirehub.exception.ForbiddenEditException;
import com.we.hirehub.exception.ResourceNotFoundException;
import com.we.hirehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ApplyRepository applyRepository;                 // ✅ 기존
    private final FavoriteCompanyRepository favoriteCompanyRepository; // ⭐ 추가
    private final JobPostsRepository jobPostsRepository;               // ⭐ 추가
    private final CompanyRepository companyRepository;                 // (선택) 이름 기반이 필요할 때

    // ====== 이력서 CRUD (기존 유지) ======

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

    /**
     * 🔥 삭제 (CRUD의 D)
     */
    @Transactional
    public void delete(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUsers_Id(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("이력서를 찾을 수 없습니다."));

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

    // ====== 프로필 (기존 유지) ======

    public MyProfileDto getProfile(Long userId) {
        Users u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));

        MyProfileDto dto = new MyProfileDto();
        BeanUtils.copyProperties(u, dto);
        return dto;
    }

    @Transactional
    public MyProfileDto updateProfile(Long userId, MyProfileUpdateRequest req) {
        Users u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));

        // ✅ 허용 필드만 부분 갱신 (email, id, password 등은 제외)
        var allowed = java.util.Set.of(
                "name","phone","birth","age","gender","address",
                "region","position","career","education"
        );

        BeanWrapper target = new BeanWrapperImpl(u);
        BeanWrapper source = new BeanWrapperImpl(req);

        for (var pd : source.getPropertyDescriptors()) {
            String name = pd.getName();
            if ("class".equals(name)) continue;
            if (!allowed.contains(name)) continue;      // 🔒 화이트리스트
            if (!target.isWritableProperty(name)) continue;

            Object val = source.getPropertyValue(name);
            if (val != null) {
                try { target.setPropertyValue(name, val); } catch (Exception ignore) {}
            }
        }

        // 타임스탬프 필드가 있으면 갱신
        if (target.isWritableProperty("updateAt")) {
            target.setPropertyValue("updateAt", LocalDate.now());
        } else if (target.isWritableProperty("updatedAt")) {
            target.setPropertyValue("updatedAt", java.time.LocalDateTime.now());
        }

        Users saved = userRepository.save(u);

        MyProfileDto dto = new MyProfileDto();
        BeanUtils.copyProperties(saved, dto);
        return dto;
    }

    // ====== 지원내역 조회 (기존 유지) ======

    public List<ApplyResponse> getMyApplyList(Long userId) {
        List<Apply> applies = applyRepository.findByResume_Users_Id(userId);

        return applies.stream()
                .map(a -> new ApplyResponse(
                        a.getId(),
                        a.getJobPosts().getCompany().getName(),
                        a.getResume().getTitle(),
                        a.getApplyAt()
                ))
                .collect(Collectors.toList());
    }

    // ===== 즐겨찾기: 추가 C =====
    @Transactional
    public FavoriteCompanySummaryDto addFavoriteCompany(Long userId, Long companyId) {
        // 유저/회사 존재 체크
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("회사를 찾을 수 없습니다. id=" + companyId));

        // 중복이면 그대로 반환(멱등)
        var existed = favoriteCompanyRepository.findByUsers_IdAndCompany_Id(userId, companyId)
                .orElse(null);
        if (existed != null) return toSummary(existed);

        // 새로 저장
        var fav = new FavoriteCompany();
        fav.setUsers(user);
        fav.setCompany(company);
        var saved = favoriteCompanyRepository.save(fav);

        return toSummary(saved);
    }

    // ===== 즐겨찾기: 목록 R (이미 있을 경우 이 메서드와 시그니처 맞춰 사용) =====
    public PagedResponse<FavoriteCompanySummaryDto> listFavoriteCompanies(Long userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        var p = favoriteCompanyRepository.findByUsers_Id(userId, pageable);
        var items = p.getContent().stream().map(this::toSummary).toList();
        return new PagedResponse<>(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    // ===== 즐겨찾기: 삭제 D (이미 있으면 그대로 사용) =====
    @Transactional
    public void removeFavoriteCompany(Long userId, Long companyId) {
        favoriteCompanyRepository.deleteByUsers_IdAndCompany_Id(userId, companyId);
    }


    // ===== 변환 =====
    private FavoriteCompanySummaryDto toSummary(FavoriteCompany fc) {
        var company = fc.getCompany();
        long openCount = (company != null && company.getId() != null)
                ? jobPostsRepository.countByCompany_Id(company.getId())
                : 0L;

        // ✅ @Builder 미사용 → 생성자 사용
        return new FavoriteCompanySummaryDto(
                fc.getId(),                              // favoriteId
                company != null ? company.getId() : null,// companyId
                company != null ? company.getName() : null,
                openCount                                // openPostCount
        );
    }
}