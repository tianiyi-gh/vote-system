package com.dzvote.vote.controller;

import com.dzvote.vote.entity.Activity;
import com.dzvote.vote.mapper.ActivityMapper;
import com.dzvote.vote.mapper.CandidateMapper;
import com.dzvote.vote.mapper.VoteRecordMapper;
import com.dzvote.vote.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计分析控制器
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final ActivityMapper activityMapper;
    private final CandidateMapper candidateMapper;
    private final VoteRecordMapper voteRecordMapper;

    /**
     * 获取活动统计概览
     */
    @GetMapping("/activity/{activityId}/overview")
    public Result<Map<String, Object>> getActivityOverview(@PathVariable Long activityId) {
        try {
            Activity activity = activityMapper.findById(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            // 获取候选人列表
            List<Map<String, Object>> candidates = candidateMapper.findByActivityId(activityId)
                .stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("votes", c.getTotalVotes());
                    return map;
                })
                .toList();

            Map<String, Object> result = new HashMap<>();
            result.put("activity", activity);
            result.put("candidates", candidates);
            result.put("candidateCount", candidates.size());
            result.put("totalVotes", candidates.stream().mapToLong(c -> (Long) ((Map<?, ?>) c).get("votes")).sum());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取投票趋势（每日统计）
     */
    @GetMapping("/activity/{activityId}/trend")
    public Result<List<Map<String, Object>>> getVoteTrend(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "7") Integer days) {

        try {
            // 这里简化实现，实际可以使用更复杂的SQL
            LocalDate startDate = LocalDate.now().minusDays(days);
            LocalDate endDate = LocalDate.now();

            // 使用VoteRecordMapper的按日期统计功能
            List<Map<String, Object>> trend = java.util.stream.IntStream.range(0, days)
                .mapToObj(i -> {
                    LocalDate date = startDate.plusDays(i);
                    Map<String, Object> dayStat = new HashMap<>();
                    dayStat.put("date", date.toString());
                    try {
                        VoteRecordMapper.VoteRecordStats stats =
                            voteRecordMapper.countActivityDailyVotes(activityId, date);
                        dayStat.put("totalVotes", stats.getTotalVotes());
                    } catch (Exception e) {
                        dayStat.put("totalVotes", 0);
                    }
                    return dayStat;
                })
                .toList();

            return Result.success(trend);
        } catch (Exception e) {
            return Result.error("获取投票趋势失败: " + e.getMessage());
        }
    }

    /**
     * 获取候选人排名
     */
    @GetMapping("/activity/{activityId}/ranking")
    public Result<List<Map<String, Object>>> getCandidateRanking(@PathVariable Long activityId) {
        try {
            List<Map<String, Object>> ranking = candidateMapper.findByActivityId(activityId)
                .stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("votes", c.getTotalVotes());
                    return map;
                })
                .toList();

            // 按票数降序排序
            ranking.sort((a, b) -> ((Long) b.get("votes")).compareTo((Long) a.get("votes")));

            return Result.success(ranking);
        } catch (Exception e) {
            return Result.error("获取排名失败: " + e.getMessage());
        }
    }

    /**
     * 获取投票来源统计
     */
    @GetMapping("/activity/{activityId}/sources")
    public Result<Map<String, Object>> getVoteSources(@PathVariable Long activityId) {
        try {
            VoteRecordMapper.VoteRecordStats stats =
                voteRecordMapper.countActivityDailyVotes(activityId, LocalDate.now());

            Map<String, Object> sources = new HashMap<>();
            sources.put("sms", stats.getSmsVotes());
            sources.put("ivr", stats.getIvrVotes());
            sources.put("web", stats.getWebVotes());
            sources.put("app", stats.getAppVotes());
            sources.put("wechat", stats.getWechatVotes());
            sources.put("pay", stats.getPayVotes());
            sources.put("total", stats.getTotalVotes());

            return Result.success(sources);
        } catch (Exception e) {
            return Result.error("获取来源统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有活动概览
     */
    @GetMapping("/activities/overview")
    public Result<List<Map<String, Object>>> getAllActivitiesOverview() {
        try {
            List<Activity> activities = activityMapper.findAll();

            List<Map<String, Object>> overview = activities.stream()
                .map(activity -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", activity.getId());
                    map.put("title", activity.getTitle());
                    map.put("status", activity.getStatus());
                    map.put("candidateCount", activity.getCandidateCount());
                    map.put("totalVotes", activity.getTotalVotes());
                    map.put("startTime", activity.getStartTime());
                    map.put("endTime", activity.getEndTime());
                    return map;
                })
                .toList();

            return Result.success(overview);
        } catch (Exception e) {
            return Result.error("获取概览失败: " + e.getMessage());
        }
    }
}
