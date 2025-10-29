package com.we.hirehub.controller;

import com.we.hirehub.dto.ReviewDto;
import com.we.hirehub.entity.Company;
import com.we.hirehub.entity.Review;
import com.we.hirehub.repository.CompanyRepository;
import com.we.hirehub.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewRestController {

    private final ReviewService reviewService;
    private final CompanyRepository companyRepository;

    /** 리뷰 등록 */
    @PostMapping
    public ReviewDto createReview(@RequestBody ReviewDto dto) {
        Review saved = reviewService.addReview(dto);
        return ReviewDto.builder()
                .id(saved.getId())
                .score(saved.getScore())
                .content(saved.getContent())
                .usersId(saved.getUsers().getId())
                .nickname(saved.getUsers().getNickname())
                .companyId(saved.getCompany().getId())
                .build();
    }

    /** 전체 리뷰 조회 */
    @GetMapping
    public List<ReviewDto> getAllReviews() {
        return reviewService.getAllReviews();
    }

    /** 특정 회사 리뷰 조회 */
    @GetMapping("/company/{companyName}")
    public ResponseEntity<List<ReviewDto>> getReviewsByCompany(@PathVariable String companyName) {
        List<Company> companies = companyRepository.findByName(companyName);
        if (companies.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Company company = companies.get(0);
        List<ReviewDto> reviews = reviewService.getReviewsByCompany(company.getId());
        return ResponseEntity.ok(reviews);
    }

    /** 특정 회사 평균 별점 조회 */
    @GetMapping("/company/{companyName}/average")
    public ResponseEntity<Double> getAverageScore(@PathVariable String companyName) {
        List<Company> companies = companyRepository.findByName(companyName);
        if (companies.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Company company = companies.get(0);
        Double avgScore = reviewService.getAverageScore(company.getId());
        return ResponseEntity.ok(avgScore);
    }
}
