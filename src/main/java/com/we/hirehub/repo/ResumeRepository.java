package com.we.hirehub.repo;

import com.we.hirehub.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUsers_Id(Long userId);
}
