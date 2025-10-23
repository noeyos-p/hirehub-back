package com.we.hirehub.service;

import com.we.hirehub.dto.JobPostsDto;
import com.we.hirehub.entity.JobPosts;
import com.we.hirehub.repository.JobPostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostServiceImpl implements JobPostsService {

    private final JobPostsRepository jobPostRepository;

    private JobPostsDto convertToDto(JobPosts job) {
        return JobPostsDto.builder()
                .id(job.getId())
                .title(job.getTitle())
                .content(job.getContent())
                .startAt(job.getStartAt())
                .endAt(job.getEndAt())
                .location(job.getLocation())
                .careerLevel(job.getCareerLevel())
                .education(job.getEducation())
                .type(job.getType())
                .salary(job.getSalary())
                .companyName(job.getCompany().getName())
                .build();
    }

    @Override
    public List<JobPostsDto> getAllJobPosts() {
        return jobPostRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public JobPostsDto getJobPostById(Long id) {
        return jobPostRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("해당 공고를 찾을 수 없습니다."));
    }

    @Override
    public List<JobPostsDto> searchJobPosts(String keyword) {
        return jobPostRepository.findByTitleContaining(keyword)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
