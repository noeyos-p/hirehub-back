package com.we.hirehub.repository;

import com.we.hirehub.entity.Apply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplyRepository extends JpaRepository<Apply, Long> {

    // ✅ 유저 → 이력서(Resume) → Apply 간접 연결
    List<Apply> findByResume_Users_Id(Long userId);
}
