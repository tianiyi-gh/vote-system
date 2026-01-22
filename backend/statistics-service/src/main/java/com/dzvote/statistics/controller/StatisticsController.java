package com.dzvote.statistics.controller;

import com.dzvote.statistics.dto.ActivityStatisticsDTO;
import com.dzvote.statistics.entity.VoteStatistics;
import com.dzvote.statistics.service.StatisticsService;
import com.dzvote.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 统计控制器
 */
@Tag(name = "统计分析", description = "统计数据相关接口")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取活动统计信息")
    @GetMapping("/activity/{activityId}")
    public Result<ActivityStatisticsDTO> getActivityStatistics(
            @PathVariable @NotNull Long activityId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        ActivityStatisticsDTO statistics = statisticsService.getActivityStatistics(activityId, startDate, endDate);
        return Result.success(statistics);
    }

    @Operation(summary = "获取实时统计数据")
    @GetMapping("/realtime/{activityId}")
    public Result<ActivityStatisticsDTO> getRealTimeStatistics(@PathVariable @NotNull Long activityId) {
        ActivityStatisticsDTO statistics = statisticsService.getRealTimeStatistics(activityId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取每日统计数据")
    @GetMapping("/daily/{activityId}")
    public Result<List<VoteStatistics>> getDailyStatistics(
            @PathVariable @NotNull Long activityId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        List<VoteStatistics> statistics = statisticsService.getDailyStatistics(activityId, startDate, endDate);
        return Result.success(statistics);
    }

    @Operation(summary = "获取候选人排名TOP N")
    @GetMapping("/top-candidates/{activityId}")
    public Result<List<ActivityStatisticsDTO.CandidateRankingDTO>> getTopCandidates(
            @PathVariable @NotNull Long activityId,
            @RequestParam(defaultValue = "10") Integer topN) {
        
        List<ActivityStatisticsDTO.CandidateRankingDTO> topCandidates = 
                statisticsService.getTopCandidates(activityId, topN);
        return Result.success(topCandidates);
    }

    @Operation(summary = "获取投票趋势数据")
    @GetMapping("/trend/{activityId}")
    public Result<List<ActivityStatisticsDTO.TrendDataDTO>> getVoteTrend(
            @PathVariable @NotNull Long activityId,
            @RequestParam(defaultValue = "7") Integer days) {
        
        List<ActivityStatisticsDTO.TrendDataDTO> trend = 
                statisticsService.getVoteTrend(activityId, days);
        return Result.success(trend);
    }

    @Operation(summary = "生成当日统计数据")
    @PostMapping("/generate/daily/{activityId}")
    public Result<Void> generateDailyStatistics(
            @PathVariable @NotNull Long activityId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        LocalDate statDate = date != null ? date : LocalDate.now();
        statisticsService.generateDailyStatistics(activityId, statDate);
        return Result.success("统计生成完成", null);
    }

    @Operation(summary = "批量生成统计数据")
    @PostMapping("/generate/batch/{activityId}")
    public Result<Void> generateBatchStatistics(
            @PathVariable @NotNull Long activityId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        statisticsService.generateBatchStatistics(activityId, startDate, endDate);
        return Result.success("批量统计生成完成", null);
    }

    @Operation(summary = "清理过期统计数据")
    @PostMapping("/clean")
    public Result<Void> cleanExpiredStatistics(@RequestParam(defaultValue = "365") Integer days) {
        statisticsService.cleanExpiredStatistics(days);
        return Result.success("清理完成", null);
    }
}