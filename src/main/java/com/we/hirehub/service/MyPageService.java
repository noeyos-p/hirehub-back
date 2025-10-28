package com.we.hirehub.service;

import com.we.hirehub.dto.*;
import com.we.hirehub.entity.*;
import com.we.hirehub.exception.ForbiddenEditException;
import com.we.hirehub.exception.ResourceNotFoundException;
import com.we.hirehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 마이페이지 서비스
 * - 이력서 CRUD
 * - 내 프로필 조회 및 수정
 * - 지원내역 조회
 * - 즐겨찾기(기업/공고) 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    // ===== Repository 의존성 주입 =====
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ApplyRepository applyRepository;
    private final FavoriteCompanyRepository favoriteCompanyRepository;
    private final JobPostsRepository jobPostsRepository;
    private final CompanyRepository companyRepository;

    /* ==========================================================
       =============== [1] 이력서 CRUD 관련 로직 ===============
       ========================================================== */

    /**
     * ✅ 이력서 목록 조회
     */
    public PagedResponse<ResumeDto> list(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateAt"));
        Page<Resume> p = resumeRepository.findByUsers_Id(userId, pageable);

        return new PagedResponse<>(
                p.getContent().stream().map(this::toDto).collect(Collectors.toList()),
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()
        );
    }

    /**
     * ✅ 이력서 단건 조회
     */
    public ResumeDto get(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUsers_Id(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("이력서를 찾을 수 없습니다."));
        return toDto(resume);
    }

    /**
     * ✅ 이력서 생성
     */
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

    /**
     * ✅ 이력서 수정
     */
    @Transactional
    public ResumeDto update(Long userId, Long resumeId, ResumeUpsertRequest req) {
        Resume resume = resumeRepository.findByIdAndUsers_Id(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("이력서를 찾을 수 없습니다."));

        // 이미 제출된 이력서는 수정 금지
        if (resume.isLocked() || resumeRepository.existsByIdAndUsers_IdAndLockedTrue(resumeId, userId)) {
            throw new ForbiddenEditException("이미 제출된 이력서는 수정할 수 없습니다.");
        }

        // 내용 갱신
        resume.setTitle(req.title());
        resume.setIdPhoto(req.idPhoto());
        resume.setEssayTittle(req.essayTitle());
        resume.setEssayContent(req.essayContent());
        resume.setUpdateAt(LocalDate.now());

        Resume updated = resumeRepository.save(resume);
        return toDto(updated);
    }

    /**
     * ✅ 이력서 삭제
     */
    @Transactional
    public void delete(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUsers_Id(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("이력서를 찾을 수 없습니다."));

        // 잠금된 이력서는 삭제 불가
        if (resume.isLocked() || resumeRepository.existsByIdAndUsers_IdAndLockedTrue(resumeId, userId)) {
            throw new ForbiddenEditException("이미 제출된 이력서는 삭제할 수 없습니다.");
        }

        resumeRepository.delete(resume);
    }

    /**
     * ✅ Resume → ResumeDto 변환
     *    + 사용자의 온보딩(프로필) 데이터(UserProfileMiniDto) 추가
     */
    private ResumeDto toDto(Resume r) {
        Users u = r.getUsers();
        UserProfileMiniDto profile = null;

        // 🧩 온보딩(회원) 정보 매핑
        if (u != null) {
            profile = new UserProfileMiniDto(
                    u.getId(),
                    u.getNickname(),
                    u.getName(),
                    u.getPhone(),
                    u.getGender(),
                    (u.getDob() != null ? LocalDate.parse(u.getDob()) : null),
                    u.getAddress(),
                    u.getEmail()
            );
        }

        // 🧩 ResumeDto 반환 (기존 + profile 추가)
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
                profile // ✅ 온보딩 값 포함
        );
    }

    /* ==========================================================
       =============== [2] 내 프로필 (온보딩 데이터) ===============
       ========================================================== */

    private LocalDate parseDob(String dob) {
        if (dob == null || dob.isBlank()) return null;
        try {
            return LocalDate.parse(dob);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer calcAge(LocalDate birth) {
        if (birth == null) return null;
        var today = LocalDate.now();
        int age = today.getYear() - birth.getYear();
        if (today.getDayOfYear() < birth.getDayOfYear()) age--;
        return Math.max(age, 0);
    }

    /**
     * ✅ 프로필 조회 (온보딩 데이터)
     */
    public MyProfileDto getProfile(Long userId) {
        Users u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));

        MyProfileDto dto = new MyProfileDto();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setNickname(u.getNickname());
        dto.setName(u.getName());
        dto.setPhone(u.getPhone());
        dto.setGender(u.getGender());
        dto.setAddress(u.getAddress());
        dto.setPosition(u.getPosition());
        dto.setEducation(u.getEducation());

        LocalDate birth = parseDob(u.getDob());
        dto.setBirth(birth);
        dto.setAge(calcAge(birth));

        dto.setRegion(u.getLocation());
        dto.setCareer(u.getCareerLevel());
        return dto;
    }

    /**
     * ✅ 프로필 수정 (온보딩 데이터 수정)
     */
    @Transactional
    public MyProfileDto updateProfile(Long userId, MyProfileUpdateRequest req) {
        Users u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));

        if (req.getNickname() != null) u.setNickname(req.getNickname());
        if (req.getName() != null) u.setName(req.getName());
        if (req.getPhone() != null) u.setPhone(req.getPhone());
        if (req.getGender() != null) u.setGender(req.getGender());
        if (req.getAddress() != null) u.setAddress(req.getAddress());
        if (req.getPosition() != null) u.setPosition(req.getPosition());
        if (req.getEducation() != null) u.setEducation(req.getEducation());
        if (req.getBirth() != null) u.setDob(req.getBirth().toString());
        if (req.getRegion() != null) u.setLocation(req.getRegion());
        if (req.getCareer() != null) u.setCareerLevel(req.getCareer());

        Users saved = userRepository.save(u);
        return getProfile(saved.getId());
    }

    /* ==========================================================
       =============== [3] 지원내역 관련 로직 ===============
       ========================================================== */

    /**
     * ✅ 내가 지원한 내역 리스트 조회
     */
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

    /* ==========================================================
       =============== [4] 기업 즐겨찾기 CRUD ===============
       ========================================================== */

    /**
     * ✅ 즐겨찾기 추가 (기업)
     */
    @Transactional
    public FavoriteCompanySummaryDto addFavoriteCompany(Long userId, Long companyId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("회사를 찾을 수 없습니다. id=" + companyId));

        var existed = favoriteCompanyRepository.findByUsers_IdAndCompany_Id(userId, companyId)
                .orElse(null);
        if (existed != null) return toSummary(existed);

        var fav = new FavoriteCompany();
        fav.setUsers(user);
        fav.setCompany(company);
        var saved = favoriteCompanyRepository.save(fav);

        return toSummary(saved);
    }

    /**
     * ✅ 즐겨찾기 목록 조회 (기업)
     */
    public PagedResponse<FavoriteCompanySummaryDto> listFavoriteCompanies(Long userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        var p = favoriteCompanyRepository.findByUsers_Id(userId, pageable);
        var items = p.getContent().stream().map(this::toSummary).toList();
        return new PagedResponse<>(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    /**
     * ✅ 즐겨찾기 삭제 (기업)
     */
    @Transactional
    public void removeFavoriteCompany(Long userId, Long companyId) {
        favoriteCompanyRepository.deleteByUsers_IdAndCompany_Id(userId, companyId);
    }

    /**
     * 즐겨찾기 엔티티 → DTO 변환
     */
    private FavoriteCompanySummaryDto toSummary(FavoriteCompany fc) {
        var company = fc.getCompany();
        long openCount = (company != null && company.getId() != null)
                ? jobPostsRepository.countByCompany_Id(company.getId())
                : 0L;

        return new FavoriteCompanySummaryDto(
                fc.getId(),
                company != null ? company.getId() : null,
                company != null ? company.getName() : null,
                openCount
        );
    }

    /* ==========================================================
       =============== [5] 채용공고 지원 로직 ===============
       ========================================================== */

    /**
     * ✅ 채용공고 지원
     *    - 지원 시 이력서를 잠금 처리(lock)
     */
    @Transactional
    public ApplyResponse applyToJob(Long userId, Long jobPostId, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUsers_Id(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("이력서를 찾을 수 없습니다."));

        JobPosts jobPost = jobPostsRepository.findById(jobPostId)
                .orElseThrow(() -> new ResourceNotFoundException("공고를 찾을 수 없습니다."));

        resume.setLocked(true);
        resumeRepository.save(resume);

        Apply apply = Apply.builder()
                .resume(resume)
                .jobPosts(jobPost)
                .applyAt(LocalDate.now())
                .build();

        Apply saved = applyRepository.save(apply);

        return new ApplyResponse(
                saved.getId(),
                jobPost.getCompany().getName(),
                resume.getTitle(),
                saved.getApplyAt()
        );
    }
}
