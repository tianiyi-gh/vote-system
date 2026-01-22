package com.dzvote.candidate.controller;

import com.dzvote.candidate.entity.Candidate;
import com.dzvote.candidate.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 候选人管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Tag(name = "候选人管理", description = "候选人相关接口")
public class CandidateController {
    
    private final CandidateService candidateService;
    
    @GetMapping
    @Operation(summary = "获取候选人列表")
    public ResponseEntity<List<Candidate>> listCandidates(
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<Candidate> candidates = candidateService.listCandidates(activityId, page, size);
            return ResponseEntity.ok(candidates);
        } catch (Exception e) {
            log.error("获取候选人列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取候选人详情")
    public ResponseEntity<Candidate> getCandidate(@PathVariable Long id) {
        try {
            Candidate candidate = candidateService.getCandidate(id);
            if (candidate != null) {
                return ResponseEntity.ok(candidate);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取候选人详情失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    @Operation(summary = "创建候选人")
    public ResponseEntity<Candidate> createCandidate(@RequestBody Candidate candidate) {
        try {
            log.info("创建候选人: activityId={}, name={}, candidateNo={}, serviceId={}", 
                     candidate.getActivityId(), candidate.getName(), candidate.getCandidateNo(), candidate.getServiceId());
            Candidate saved = candidateService.createCandidate(candidate);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("创建候选人失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新候选人")
    public ResponseEntity<Candidate> updateCandidate(
            @PathVariable Long id,
            @RequestBody Candidate candidate) {
        try {
            candidate.setId(id);
            log.info("更新候选人: id={}, activityId={}, name={}, candidateNo={}, serviceId={}", 
                     id, candidate.getActivityId(), candidate.getName(), candidate.getCandidateNo(), candidate.getServiceId());
            Candidate updated = candidateService.updateCandidate(candidate);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("更新候选人失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除候选人")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        try {
            boolean deleted = candidateService.deleteCandidate(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("删除候选人失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
