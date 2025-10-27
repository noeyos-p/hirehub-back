package com.we.hirehub.controller;

import com.we.hirehub.dto.BoardDto;
import com.we.hirehub.entity.Board;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repository.UsersRepository;
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
    private final UsersRepository usersRepository; // ✅ 유저 조회용 추가

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
                                         @AuthenticationPrincipal Long userId) { // ✅ userId로 받기
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인된 사용자만 게시글을 작성할 수 있습니다.");
        }

        try {
            // ✅ userId를 통해 Users 엔티티 로드
            Users loggedInUser = usersRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            // ✅ 로그인한 유저로 게시글 생성
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
