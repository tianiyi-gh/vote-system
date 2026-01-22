package com.dzvote.vote.controller;

import com.dzvote.vote.dto.ReportExportRequest;
import com.dzvote.vote.entity.Activity;
import com.dzvote.vote.entity.Candidate;
import com.dzvote.vote.mapper.ActivityMapper;
import com.dzvote.vote.mapper.CandidateMapper;
import com.dzvote.vote.mapper.VoteRecordMapper;
import com.dzvote.vote.mapper.VoteRecordMapper.VoteRecordStats;
import com.dzvote.vote.service.ExportService;
import com.dzvote.vote.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台专用控制器
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ActivityMapper activityMapper;
    private final CandidateMapper candidateMapper;
    private final VoteRecordMapper voteRecordMapper;
    private final ExportService exportService;

    /**
     * 获取系统概览统计
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        try {
            List<Activity> activities = activityMapper.findAll();

            int totalActivities = activities.size();
            int activeActivities = (int) activities.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 1)
                .count();

            int totalCandidates = activities.stream()
                .mapToInt(a -> a.getCandidateCount() != null ? a.getCandidateCount() : 0)
                .sum();

            int totalVotes = activities.stream()
                .mapToInt(a -> a.getTotalVotes() != null ? a.getTotalVotes() : 0)
                .sum();

            Map<String, Object> overview = new HashMap<>();
            overview.put("totalActivities", totalActivities);
            overview.put("activeActivities", activeActivities);
            overview.put("totalCandidates", totalCandidates);
            overview.put("totalVotes", totalVotes);

            return Result.success(overview);
        } catch (Exception e) {
            return Result.error("获取概览统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动统计详情
     */
    @GetMapping("/statistics/{activityId}")
    public Result<Map<String, Object>> getActivityStatistics(@PathVariable Long activityId) {
        try {
            // 获取活动信息
            Activity activity = activityMapper.findById(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            // 获取候选人列表
            List<Candidate> candidates = candidateMapper.findByActivityId(activityId);

            // 获取今日统计
            LocalDate today = LocalDate.now();
            VoteRecordStats dailyStats = voteRecordMapper.countActivityDailyVotes(activityId, today);
            int uniqueVoters = voteRecordMapper.countActivityUniqueVoters(activityId, today);

            // 计算总数
            int totalVotes = candidates.stream()
                .mapToInt(c -> c.getTotalVotes() != null ? c.getTotalVotes().intValue() : 0)
                .sum();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("activityId", activityId);
            statistics.put("activityTitle", activity.getTitle());
            statistics.put("totalVotes", totalVotes);
            statistics.put("uniqueVoters", uniqueVoters);
            statistics.put("candidates", candidates);
            statistics.put("dailyVotes", dailyStats != null ? dailyStats.getTotalVotes() : 0);

            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("获取统计详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取投票趋势数据（支持时间范围）
     */
    @GetMapping("/trends/{activityId}")
    public Result<List<Map<String, Object>>> getVoteTrends(
            @PathVariable Long activityId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        try {
            Activity activity = activityMapper.findById(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            List<Map<String, Object>> trends;
            if (startDate != null && endDate != null) {
                // 根据指定时间范围获取趋势
                trends = voteRecordMapper.getActivityVoteTrendsByRange(
                    activityId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            } else {
                // 获取最近N天的投票趋势
                trends = voteRecordMapper.getActivityVoteTrends(activityId, days);
            }

            return Result.success(trends);
        } catch (Exception e) {
            return Result.error("获取投票趋势失败: " + e.getMessage());
        }
    }

    /**
     * 更新投票限制配置
     */
    @PostMapping("/settings/ratelimit")
    public Result<String> updateRateLimitSettings(@RequestBody Map<String, Object> settings) {
        try {
            // TODO: 实现将配置保存到数据库或配置文件
            // 当前仅返回成功
            return Result.success("设置保存成功");
        } catch (Exception e) {
            return Result.error("保存设置失败: " + e.getMessage());
        }
    }

    /**
     * 获取投票限制配置
     */
    @GetMapping("/settings/ratelimit")
    public Result<Map<String, Object>> getRateLimitSettings() {
        try {
            Map<String, Object> settings = new HashMap<>();
            // TODO: 从数据库或配置文件读取配置
            settings.put("voteIpLimit", 1);
            settings.put("rateLimitCount", 10);
            settings.put("enableCaptcha", false);

            return Result.success(settings);
        } catch (Exception e) {
            return Result.error("获取设置失败: " + e.getMessage());
        }
    }

    /**
     * 获取候选人得票率统计
     */
    @GetMapping("/candidates/vote-rate/{activityId}")
    public Result<List<Map<String, Object>>> getCandidateVoteRates(@PathVariable Long activityId) {
        try {
            Activity activity = activityMapper.findById(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            List<Map<String, Object>> voteRates = voteRecordMapper.getCandidateVoteRates(activityId);
            return Result.success(voteRates);
        } catch (Exception e) {
            return Result.error("获取得票率统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动参与度分析
     */
    @GetMapping("/participation/{activityId}")
    public Result<List<Map<String, Object>>> getHourlyParticipation(
            @PathVariable Long activityId,
            @RequestParam(required = false) String date) {
        try {
            Activity activity = activityMapper.findById(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            List<Map<String, Object>> participation = voteRecordMapper.getHourlyParticipation(activityId, queryDate);
            return Result.success(participation);
        } catch (Exception e) {
            return Result.error("获取参与度分析失败: " + e.getMessage());
        }
    }

    /**
     * 导出活动报表
     */
    @PostMapping("/export")
    public void exportActivityReport(@RequestBody ReportExportRequest request,
                                    HttpServletResponse response) throws IOException {
        try {
            // 验证活动存在
            Activity activity = activityMapper.findById(request.getActivityId());
            if (activity == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"活动不存在\"}");
                return;
            }

            // 根据报表类型获取数据
            List<Map<String, Object>> data;
            switch (request.getReportType()) {
                case "VOTE_RECORDS":
                    data = voteRecordMapper.findVoteRecordsByActivity(
                        request.getActivityId(),
                        request.getStartDate(),
                        request.getEndDate());
                    break;
                case "CANDIDATE_STATS":
                    data = voteRecordMapper.getCandidateVoteRates(request.getActivityId());
                    break;
                case "ACTIVITY_SUMMARY":
                    Activity act = activityMapper.findById(request.getActivityId());
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("id", act.getId());
                    summary.put("title", act.getTitle());
                    summary.put("total_votes", act.getTotalVotes());
                    summary.put("candidate_count", act.getCandidateCount());
                    summary.put("start_time", act.getStartTime());
                    summary.put("end_time", act.getEndTime());
                    summary.put("status", act.getStatus());
                    data = List.of(summary);
                    break;
                default:
                    data = List.of();
            }

            // 导出报表
            exportService.exportReport(request, response, data);

        } catch (Exception e) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"导出报表失败: " + e.getMessage() + "\"}");
        }
    }
}
