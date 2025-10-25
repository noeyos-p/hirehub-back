package com.we.hirehub.controller;

import com.we.hirehub.dto.ReviewDto;
import com.we.hirehub.entity.Review;
import com.we.hirehub.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewRestController {

    private final ReviewService reviewService;

    @PostMapping
    public Review createReview(@RequestBody ReviewDto dto) {
        return reviewService.addReview(dto);
    }


    /** 전체 리뷰 조회 */
    @GetMapping
    public List<ReviewDto> getAllReviews() {
        return reviewService.getAllReviews();
    }

    /** 특정 회사 리뷰 조회 */
    @GetMapping("/company/{companyId}")
    public List<ReviewDto> getReviewsByCompany(@PathVariable Long companyId) {
        return reviewService.getReviewsByCompany(companyId);
    }
}
