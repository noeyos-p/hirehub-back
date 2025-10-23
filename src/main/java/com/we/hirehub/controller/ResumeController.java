package com.we.hirehub.controller;

import com.we.hirehub.dto.ResumeDto;
import com.we.hirehub.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<ResumeDto> createResume(@RequestBody ResumeDto dto) {
        return ResponseEntity.ok(resumeService.create(dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResumeDto>> getResumesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(resumeService.findByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDto> getResume(@PathVariable Long id) {
        return ResponseEntity.ok(resumeService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeDto> updateResume(@PathVariable Long id, @RequestBody ResumeDto dto) {
        return ResponseEntity.ok(resumeService.update(id, dto));
    }
}
