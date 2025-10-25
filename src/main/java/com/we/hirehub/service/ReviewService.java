package com.we.hirehub.service;

import com.we.hirehub.dto.ReviewDto;
import com.we.hirehub.entity.Company;
import com.we.hirehub.entity.Review;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repository.CompanyRepository;
import com.we.hirehub.repository.ReviewRepository;
import com.we.hirehub.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UsersRepository usersRepository;
    private final CompanyRepository companyRepository;

    public Review addReview(ReviewDto dto) {
        Users user = usersRepository.findById(dto.getUsersId())
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("회사 정보가 존재하지 않습니다."));

        Review review = Review.builder()
                .score(dto.getScore())
                .content(dto.getContent())
                .users(user)
                .company(company)
                .build();

        return reviewRepository.save(review);
    }


    /** 전체 리뷰 조회 */
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /** 특정 회사 리뷰 조회 */
    public List<ReviewDto> getReviewsByCompany(Long companyId) {
        return reviewRepository.findByCompanyId(companyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /** 엔티티 → DTO 변환 */
    private ReviewDto convertToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .score(review.getScore())
                .content(review.getContent())
                .usersId(review.getUsers().getId())
                .companyId(review.getCompany().getId())
                .build();
    }
}
