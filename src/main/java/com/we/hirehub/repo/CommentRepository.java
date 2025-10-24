package com.we.hirehub.repo;

import com.we.hirehub.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {
    List<Comments> findByBoardId(Long boardId);
    void deleteByParentComments(Comments parentComments); // 추가
    // 수정
    List<Comments> findByParentComments_Id(Long parentId);
    List<Comments> findByBoardIdOrderByCreateAtAsc(Long boardId);
}
