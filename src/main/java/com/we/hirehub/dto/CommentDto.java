package com.we.hirehub.dto;

import com.we.hirehub.entity.Comments;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private String content;
    private Long usersId;
    private String usersName;        // 작성자 이름
    private Long boardId;
    private Long parentCommentId;    // 상위 댓글 ID
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    /** 🔹 엔티티 → DTO 변환 */
    public static CommentDto fromEntity(Comments comment) {
        if (comment == null) return null;

        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .usersId(comment.getUsers() != null ? comment.getUsers().getId() : null)
                .usersName(comment.getUsers() != null ? comment.getUsers().getName() : null)
                .boardId(comment.getBoard() != null ? comment.getBoard().getId() : null)
                .parentCommentId(comment.getParentComments() != null ? comment.getParentComments().getId() : null)
                .createAt(comment.getCreateAt())
                .updateAt(comment.getUpdateAt())
                .build();
    }

    /** 🔹 DTO → 엔티티 변환 (필요 시) */
    public Comments toEntity() {
        Comments comment = new Comments();
        comment.setId(this.id);
        comment.setContent(this.content);
        comment.setCreateAt(this.createAt != null ? this.createAt : LocalDateTime.now());
        comment.setUpdateAt(this.updateAt);
        // Users, Board, ParentComments는 서비스 레이어에서 set 해주는 것을 권장
        return comment;
    }
}
