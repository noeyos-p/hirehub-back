package com.we.hirehub.repository;

import com.we.hirehub.entity.JobPosts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostsRepository extends JpaRepository<JobPosts, Long> {
    List<JobPosts> findByLocationContaining(String location);
    List<JobPosts> findByCareerLevelContaining(String careerLevel);
    List<JobPosts> findByTitleContaining(String keyword);
}
