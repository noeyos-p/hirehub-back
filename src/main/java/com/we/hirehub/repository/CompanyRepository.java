package com.we.hirehub.repository;

import com.we.hirehub.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    // 필요 시 name으로 조회할 때 사용
    java.util.Optional<Company> findByName(String name);
}
