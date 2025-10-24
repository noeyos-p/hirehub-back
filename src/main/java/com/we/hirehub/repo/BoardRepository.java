package com.we.hirehub.repo;

import com.we.hirehub.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findTop5ByOrderByViewsDesc();      // 인기 게시글
    List<Board> findAllByOrderByCreateAtDesc();    // 최신순 정렬
}