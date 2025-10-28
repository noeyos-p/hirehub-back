package com.we.hirehub.repository;

import com.we.hirehub.entity.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {
    // 페이징으로 모든 댓글 조회
    Page<Comments> findAll(Pageable pageable);
    // 특정 사용자의 댓글 조회
    @Query("SELECT c FROM Comments c WHERE c.users.id = :userId ORDER BY c.createAt DESC")
    Page<Comments> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 특정 댓글에 달린 답글 조회 (대댓글)
    @Query("SELECT c FROM Comments c WHERE c.parentComments.id = :parentId ORDER BY c.createAt ASC")
    List<Comments> findRepliesByParentId(@Param("parentId") Long parentId);
    List<Comments> findByBoardId(Long boardId);
    void deleteByParentComments(Comments parentComments); // 추가
    // 수정
    List<Comments> findByParentComments_Id(Long parentId);
    List<Comments> findByBoardIdOrderByCreateAtAsc(Long boardId);
}
