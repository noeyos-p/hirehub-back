package com.we.hirehub.service;

import com.we.hirehub.dto.ResumeDto;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ResumeService {
    public ResumeDto create(ResumeDto dto){ return dto; }
    public List<ResumeDto> findByUser(Long userId){ return Collections.emptyList(); }
    public ResumeDto get(Long id){ return new ResumeDto(); }
    public ResumeDto update(Long id, ResumeDto dto){ return dto; }
}
