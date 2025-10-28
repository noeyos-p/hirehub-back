package com.we.hirehub.repository;

import com.we.hirehub.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    // 필요 시 name으로 조회할 때 사용
    List<Company> findByName(String name);
}
