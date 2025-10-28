package com.we.hirehub.service;

import com.we.hirehub.dto.FavoriteJobPostSummaryDto;
import com.we.hirehub.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostScrapService {

    private final JdbcTemplate jdbc;

    // scrap_posts 의 공고 컬럼명에 맞춰 설정: "job_posts_id" 또는 "job_post_id"
    private static final String JOB_POST_COL = "job_posts_id"; // ← 필요 시 "job_post_id" 로 변경

    private static final RowMapper<FavoriteJobPostSummaryDto> ROW = new RowMapper<>() {
        @Override public FavoriteJobPostSummaryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new FavoriteJobPostSummaryDto(
                    rs.getLong("id"),
                    rs.getLong("job_post_id"),
                    rs.getString("title"),
                    rs.getString("company_name"),
                    rs.getDate("end_at").toLocalDate()
            );
        }
    };

    /** 스크랩 추가 (멱등) */
    @Transactional
    public FavoriteJobPostSummaryDto add(Long userId, Long jobPostId) {
        // MySQL: 중복이면 무시
        String insertSql = "INSERT IGNORE INTO scrap_posts (users_id, " + JOB_POST_COL + ") VALUES (?, ?)";
        jdbc.update(insertSql, userId, jobPostId);

        String selectOne =
                "SELECT s.id, s." + JOB_POST_COL + " AS job_post_id, j.title, c.name AS company_name, j.end_at " +
                        "FROM scrap_posts s " +
                        "JOIN job_posts j ON j.id = s." + JOB_POST_COL + " " +
                        "JOIN company c ON c.id = j.company_id " +
                        "WHERE s.users_id = ? AND s." + JOB_POST_COL + " = ? " +
                        "LIMIT 1";
        return jdbc.queryForObject(selectOne, ROW, userId, jobPostId);
    }

    /** 스크랩 목록 (페이지네이션) */
    public PagedResponse<FavoriteJobPostSummaryDto> list(Long userId, int page, int size) {
        int limit  = Math.max(size, 1);
        int offset = Math.max(page, 0) * limit;

        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM scrap_posts WHERE users_id = ?",
                Long.class, userId
        );

        String listSql =
                "SELECT s.id, s." + JOB_POST_COL + " AS job_post_id, j.title, c.name AS company_name, j.end_at " +
                        "FROM scrap_posts s " +
                        "JOIN job_posts j ON j.id = s." + JOB_POST_COL + " " +
                        "JOIN company c ON c.id = j.company_id " +
                        "WHERE s.users_id = ? " +
                        "ORDER BY s.id DESC " +
                        "LIMIT ? OFFSET ?";
        List<FavoriteJobPostSummaryDto> rows = jdbc.query(listSql, ROW, userId, limit, offset);

        long totalCount = total != null ? total : 0L;
        int totalPages = (int) Math.ceil(totalCount / (double) limit);
        return new PagedResponse<>(rows, page, limit, totalCount, totalPages);
    }

    /** 스크랩 삭제 */
    @Transactional
    public void remove(Long userId, Long jobPostId) {
        String deleteSql = "DELETE FROM scrap_posts WHERE users_id = ? AND " + JOB_POST_COL + " = ?";
        jdbc.update(deleteSql, userId, jobPostId);
    }
}

