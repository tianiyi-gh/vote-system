package com.dzvote.statistics.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 活动统计DTO
 */
@Data
@Builder
public class ActivityStatisticsDTO {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 活动状态
     */
    private Integer activityStatus;

    /**
     * 统计日期范围
     */
    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * 总投票数
     */
    private Long totalVotes;

    /**
     * 各渠道票数统计
     */
    private Map<String, Long> channelVotes;

    /**
     * 独立投票人数
     */
    private Integer totalUniqueVoters;

    /**
     * 每日统计数据
     */
    private List<DailyStatisticsDTO> dailyStats;

    /**
     * 候选人排名TOP10
     */
    private List<CandidateRankingDTO> topCandidates;

    /**
     * 投票趋势数据（最近7天）
     */
    private List<TrendDataDTO> voteTrend;

    /**
     * 每日投票统计
     */
    @Data
    @Builder
    public static class DailyStatisticsDTO {
        private LocalDate date;
        private Long totalVotes;
        private Integer uniqueVoters;
        private Map<String, Long> channelVotes;
    }

    /**
     * 候选人排名
     */
    @Data
    @Builder
    public static class CandidateRankingDTO {
        private Long candidateId;
        private String candidateName;
        private String groupName;
        private Long totalVotes;
        private Integer ranking;
        private String avatar;
    }

    /**
     * 趋势数据
     */
    @Data
    @Builder
    public static class TrendDataDTO {
        private LocalDate date;
        private Long votes;
        private Integer uniqueVoters;
    }
}