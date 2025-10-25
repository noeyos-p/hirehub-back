package com.we.hirehub.service;

import com.we.hirehub.dto.JobPostsDto;

import java.util.List;

public interface JobPostsService {
    List<JobPostsDto> getAllJobPosts();
    JobPostsDto getJobPostById(Long id);
    List<JobPostsDto> searchJobPosts(String keyword);
    JobPostsDto createJobPost(JobPostsDto dto);

}
