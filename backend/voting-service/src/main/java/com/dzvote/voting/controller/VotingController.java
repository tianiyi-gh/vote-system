package com.dzvote.voting.controller;

import com.dzvote.voting.dto.VoteRequest;
import com.dzvote.voting.dto.VoteResult;
import com.dzvote.voting.service.VotingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 投票控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/voting")
@RequiredArgsConstructor
@Tag(name = "投票管理", description = "投票相关接口")
public class VotingController {
    
    private final VotingService votingService;
    
    @PostMapping
    @Operation(summary = "投票")
    public ResponseEntity<VoteResult> vote(@RequestBody VoteRequest request) {
        try {
            VoteResult result = votingService.vote(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("投票失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/check/{activityId}/{phone}")
    @Operation(summary = "检查投票限制")
    public ResponseEntity<Boolean> checkVoteLimit(
            @PathVariable Long activityId,
            @PathVariable String phone) {
        try {
            boolean canVote = votingService.checkVoteLimit(activityId, phone);
            return ResponseEntity.ok(canVote);
        } catch (Exception e) {
            log.error("检查投票限制失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
