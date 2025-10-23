package com.we.hirehub.repo;

import com.we.hirehub.entity.Apply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplyRepository extends JpaRepository<Apply, Long> {
    // 기존: List<Apply> findByUsers_Id(Long userId);  // ❌ users 필드가 Apply에 없음
    // 수정: Resume -> Users -> Id 경유
    List<Apply> findByResume_Users_Id(Long userId);     // ✅
}
