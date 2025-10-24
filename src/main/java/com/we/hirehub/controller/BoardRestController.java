package com.we.hirehub.controller;

import com.we.hirehub.dto.BoardDto;
import com.we.hirehub.entity.Board;
import com.we.hirehub.entity.Users;
import com.we.hirehub.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board") // React 전용 API 엔드포인트
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class BoardRestController {
    private final BoardService boardService;

    /** 전체 게시물 조회 */
    @GetMapping
    public List<BoardDto> getAllBoards() {
        return boardService.getAllBoards();
    }

    /** 인기 게시물 조회 */
    @GetMapping("/popular")
    public List<BoardDto> getPopularBoards() {
        return boardService.getPopularBoards();
    }

    /** 게시물 작성 */
    @PostMapping
    public ResponseEntity<?> createBoard(@RequestBody BoardDto boardDto,
                                         @AuthenticationPrincipal Users loggedInUser) {
        System.out.println("loggedInUser: " + (loggedInUser != null ? loggedInUser.getId() : "null"));
        if (loggedInUser == null) {
            System.out.println("⚠️ 로그인 안됨 - 테스트용 더미 유저 사용");
            try {
                Board board = boardService.createBoardWithUserId(
                        boardDto.getTitle(),
                        boardDto.getContent(),
                        1L  // DB에 존재하는지 확인
                );
                return ResponseEntity.ok(board);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("게시글 등록 실패: " + e.getMessage());
            }
        }
        try {
            Board board = boardService.createBoard(
                    boardDto.getTitle(),
                    boardDto.getContent(),
                    loggedInUser
            );
            return ResponseEntity.ok(board);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("게시글 등록 실패: " + e.getMessage());
        }
    }

    /** 게시물 상세 조회 + 조회수 증가 */
    @GetMapping("/{id}")
    public BoardDto getBoard(@PathVariable Long id) {
        return boardService.getBoard(id);
    }

    /** 조회수만 증가 */
    @PutMapping("/{id}/view")
    public BoardDto incrementView(@PathVariable Long id) {
        return boardService.incrementView(id);
    }
}
