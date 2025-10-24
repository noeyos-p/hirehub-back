package com.we.hirehub.repository;

import com.we.hirehub.entity.Apply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplyRepository extends JpaRepository<Apply, Long> {
    // ✅ 당분간 파생쿼리 메서드 없애두세요. (status 필드가 없어서 부팅에 실패함)
    // 필요해지면, Apply 엔티티의 ‘실제 필드명’에 맞춘 메서드로 추가하세요.
    // 예) boolean existsByResume_Id(Long resumeId);  // Resume 연관 필드가 실제로 있을 때만
}
