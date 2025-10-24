package com.we.hirehub.repository;

import com.we.hirehub.entity.JobPosts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostsRepository extends JpaRepository<JobPosts, Long> {

    // 기존 검색 메서드
    List<JobPosts> findByLocationContaining(String location);
    List<JobPosts> findByCareerLevelContaining(String careerLevel);
    List<JobPosts> findByTitleContaining(String keyword);

    // ★ 추가: 회사명으로 공고 수 카운트 (상태 필드 없이 전체 공고 수)
    long countByCompany_Id(Long companyId);

    /*
     * 만약 상태 필드가 생기면 아래처럼 바꾸는 것을 추천:
     *
     * long countByCompanyNameAndStatusIn(String companyName, Collection<JobPostStatus> statuses);
     *
     * // 사용 예: jobPostsRepository.countByCompanyNameAndStatusIn(name, List.of(OPEN, ACTIVE));
     */
}
