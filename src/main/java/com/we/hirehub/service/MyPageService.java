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
import org.springframework.data.domain.*;
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

    /** 🔥 삭제 (CRUD의 D) */
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

        BeanWrapper target = new BeanWrapperImpl(u);
        BeanWrapper source = new BeanWrapperImpl(req);

        for (var pd : source.getPropertyDescriptors()) {
            String name = pd.getName();
            if ("class".equals(name)) continue;
            if ("email".equalsIgnoreCase(name)) continue;
            if (!target.isWritableProperty(name)) continue;

            Object val = source.getPropertyValue(name);
            if (val != null) {
                try {
                    target.setPropertyValue(name, val);
                } catch (Exception ignore) {}
            }
        }

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

    // ====== ⭐ 신규: 관심 기업 ======

    /** 관심 기업 목록(회사명 + 회사 공고 수) */
    public PagedResponse<FavoriteCompanySummaryDto> listFavoriteCompanies(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<FavoriteCompany> p = favoriteCompanyRepository.findByUsers_Id(userId, pageable);

        var items = p.getContent().stream()
                .map(fc -> new FavoriteCompanySummaryDto(
                        fc.getId(),
                        fc.getCompany().getId(),
                        fc.getCompany().getName(),
                        jobPostsRepository.countByCompany_Id(fc.getCompany().getId())
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    /** 관심 기업 추가(중복 시 기존 유지) */
//    @Transactional
//    public FavoriteCompanySummaryDto addFavoriteCompany(Long userId, Long companyId) {
//        Users user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));
//
//        var company = companyRepository.findById(companyId)
//                .orElseThrow(() -> new ResourceNotFoundException("회사를 찾을 수 없습니다."));
//
//        // 이미 즐겨찾기면 그대로 반환
//        var exist = favoriteCompanyRepository.findByUsers_IdAndCompany_Id(userId, companyId);
//        if (exist.isPresent()) {
//            var fc = exist.get();
//            long cnt = jobPostsRepository.countByCompany_Id(companyId);
//            return new FavoriteCompanySummaryDto(fc.getId(), companyId, company.getName(), cnt);
//        }
//
//        // ✅ 익명서브클래스 금지. 일반 인스턴스로 생성해서 저장
//        FavoriteCompany fc = new FavoriteCompany();
//        fc.setUsers(user);
//        fc.setCompany(company);
//
//        FavoriteCompany saved = favoriteCompanyRepository.save(fc);
//
//        long cnt = jobPostsRepository.countByCompany_Id(companyId);
//        return new FavoriteCompanySummaryDto(saved.getId(), companyId, company.getName(), cnt);
//    }


    /** 관심 기업 삭제(회사 ID 기준) */
    @Transactional
    public void removeFavoriteCompany(Long userId, Long companyId) {
        var fc = favoriteCompanyRepository
                .findByUsers_IdAndCompany_Id(userId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("즐겨찾기에 해당 회사가 없습니다."));
        favoriteCompanyRepository.delete(fc);  // 이 방법은 @Modifying 불필요
    }
}
