package com.we.hirehub.controller;

import com.we.hirehub.dto.CommentDto;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repository.CommentRepository;
import com.we.hirehub.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
@CrossOrigin(origins = "http://localhost:3000") // React 앱 포트
public class CommentRestController {
    private final CommentService commentService;
    private final CommentRepository commentRepository;

    /**
     * 댓글 생성
     */
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentDto commentDto,
                                           @AuthenticationPrincipal Users loggedInUser) {
        System.out.println("POST /api/comment 요청 수신: " + commentDto);
        System.out.println("Authentication: " + (loggedInUser != null ? loggedInUser.getId() : "null"));
        try {
            if (loggedInUser == null) {
                System.out.println("⚠️ 로그인 안됨 - 테스트용 더미 유저 사용");
                CommentDto createdComment = commentService.createCommentWithUserId(
                        commentDto.getContent(),
                        commentDto.getBoardId(),
                        commentDto.getParentCommentId(),
                        1L // 실제 DB에 존재하는 userId
                );
                return ResponseEntity.ok(createdComment);
            }
            CommentDto createdComment = commentService.createComment(commentDto, loggedInUser);
            return ResponseEntity.ok(createdComment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("댓글 등록 실패: " + e.getMessage());
        }
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("댓글 삭제 실패: " + e.getMessage());
        }
    }

    /**
     * 게시글 ID로 댓글 목록 조회
     */
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<CommentDto>> getCommentsByBoardId(@PathVariable Long boardId) {
        try {
            // 모든 댓글을 반환 (대댓글 포함)
            List<CommentDto> comments = commentRepository.findByBoardId(boardId)
                    .stream()
                    .map(comment -> CommentDto.builder()
                            .id(comment.getId())
                            .content(comment.getContent())
                            .usersId(comment.getUsers() != null ? comment.getUsers().getId() : null)
                            .nickname(comment.getUsers() != null ? comment.getUsers().getNickname() : "익명")
                            .boardId(comment.getBoard() != null ? comment.getBoard().getId() : null)
                            .parentCommentId(comment.getParentComments() != null ? comment.getParentComments().getId() : null)
                            .createAt(comment.getCreateAt())
                            .build())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
