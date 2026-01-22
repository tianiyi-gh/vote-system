package com.dzvote.statistics.service;

import com.dzvote.statistics.entity.VoteStatistics;
import com.dzvote.statistics.dto.ActivityStatisticsDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 生成活动日统计
     * @param activityId 活动ID
     * @param statDate 统计日期
     */
    void generateDailyStatistics(Long activityId, LocalDate statDate);

    /**
     * 批量生成统计数据
     * @param activityId 活动ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    void generateBatchStatistics(Long activityId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取活动统计信息
     * @param activityId 活动ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    ActivityStatisticsDTO getActivityStatistics(Long activityId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取活动每日统计
     * @param activityId 活动ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日统计列表
     */
    List<VoteStatistics> getDailyStatistics(Long activityId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取候选人排名TOP N
     * @param activityId 活动ID
     * @param topN 返回数量
     * @return 候选人排名列表
     */
    List<ActivityStatisticsDTO.CandidateRankingDTO> getTopCandidates(Long activityId, Integer topN);

    /**
     * 获取投票趋势数据
     * @param activityId 活动ID
     * @param days 天数
     * @return 趋势数据
     */
    List<ActivityStatisticsDTO.TrendDataDTO> getVoteTrend(Long activityId, Integer days);

    /**
     * 获取实时投票数据
     * @param activityId 活动ID
     * @return 实时统计
     */
    ActivityStatisticsDTO getRealTimeStatistics(Long activityId);

    /**
     * 清理过期统计数据
     * @param days 保留天数
     */
    void cleanExpiredStatistics(Integer days);
}