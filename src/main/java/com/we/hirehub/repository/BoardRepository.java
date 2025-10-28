package com.we.hirehub.repository;

import com.we.hirehub.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 페이징으로 모든 게시글 조회
    Page<Board> findAll(Pageable pageable);
    // 제목 또는 내용으로 게시글 검색
    @Query("SELECT b FROM Board b WHERE b.title LIKE %:keyword% OR b.content LIKE %:keyword% ORDER BY b.createAt DESC")
    Page<Board> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    List<Board> findTop5ByOrderByViewsDesc();      // 인기 게시글
    List<Board> findAllByOrderByCreateAtDesc();    // 최신순 정렬
}