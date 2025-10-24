package com.we.hirehub.repository;

import com.we.hirehub.entity.FavoriteCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface FavoriteCompanyRepository extends JpaRepository<FavoriteCompany, Long> {

    Page<FavoriteCompany> findByUsers_Id(Long userId, Pageable pageable);

    boolean existsByUsers_IdAndCompany_Id(Long userId, Long companyId);

    Optional<FavoriteCompany> findByUsers_IdAndCompany_Id(Long userId, Long companyId);

    // 🔥 파생 deleteBy... 는 @Modifying + @Transactional 이 필수
    @Modifying
    @Transactional
    long deleteByUsers_IdAndCompany_Id(Long userId, Long companyId);
}
