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

    @PostMapping
    public Review createReview(@RequestBody ReviewDto dto) {
        return reviewService.addReview(dto);
    }


    /** 전체 리뷰 조회 */
    @GetMapping
    public List<ReviewDto> getAllReviews() {
        return reviewService.getAllReviews();
    }

    // 수정
    /** 특정 회사 리뷰 조회 */
    @GetMapping("/company/{companyName}")
    public ResponseEntity<List<ReviewDto>> getReviewsByCompany(@PathVariable String companyName) {
        // 1️⃣ 회사 이름으로 조회 (리스트로 받음)
        List<Company> companies = companyRepository.findByName(companyName);

        if (companies.isEmpty()) {
            return ResponseEntity.notFound().build(); // 회사 없으면 404
        }

        // 2️⃣ 첫 번째 회사만 사용 (DB에 같은 이름이 여러 개라도 첫 번째만)
        Company company = companies.get(0);

        // 3️⃣ 리뷰 조회
        List<ReviewDto> reviews = reviewService.getReviewsByCompany(company.getId());

        return ResponseEntity.ok(reviews);
    }
}
