package com.we.hirehub.controller;

import com.we.hirehub.dto.JobPostsDto;
import com.we.hirehub.service.JobPostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobposts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JobPostController {

    private final JobPostsService jobPostService;

    @GetMapping
    public List<JobPostsDto> getAllJobPosts() {
        return jobPostService.getAllJobPosts();
    }

    @GetMapping("/{id}")
    public JobPostsDto getJobPostById(@PathVariable Long id) {
        return jobPostService.getJobPostById(id);
    }

    @GetMapping("/search")
    public List<JobPostsDto> searchJobPosts(@RequestParam String keyword) {
        return jobPostService.searchJobPosts(keyword);
    }
}
