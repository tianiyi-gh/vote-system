package com.dzvote.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dzvote.statistics.entity.VoteStatistics;
import com.dzvote.statistics.mapper.VoteStatisticsMapper;
import com.dzvote.statistics.service.StatisticsService;
import com.dzvote.statistics.dto.ActivityStatisticsDTO;
import com.dzvote.activity.entity.Activity;
import com.dzvote.activity.entity.Candidate;
import com.dzvote.activity.service.ActivityService;
import com.dzvote.activity.mapper.CandidateMapper;
import com.dzvote.vote.mapper.VoteRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final VoteStatisticsMapper voteStatisticsMapper;
    private final VoteRecordMapper voteRecordMapper;
    private final ActivityService activityService;
    private final CandidateMapper candidateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateDailyStatistics(Long activityId, LocalDate statDate) {
        try {
            // 检查活动是否存在
            Activity activity = activityService.getById(activityId);
            if (activity == null) {
                log.warn("活动不存在: {}", activityId);
                return;
            }

            // 获取投票统计数据
            VoteRecordMapper.VoteRecordStats voteStats = voteRecordMapper.countActivityDailyVotes(activityId, statDate);
            Integer uniqueVoters = voteRecordMapper.countActivityUniqueVoters(activityId, statDate);

            // 检查是否已存在统计数据
            LambdaQueryWrapper<VoteStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VoteStatistics::getActivityId, activityId)
                       .eq(VoteStatistics::getStatDate, statDate);

            VoteStatistics existStats = voteStatisticsMapper.selectOne(queryWrapper);

            if (existStats != null) {
                // 更新已有统计
                existStats.setTotalVotes(voteStats.getTotalVotes());
                existStats.setSmsVotes(voteStats.getSmsVotes());
                existStats.setIvrVotes(voteStats.getIvrVotes());
                existStats.setWebVotes(voteStats.getWebVotes());
                existStats.setAppVotes(voteStats.getAppVotes());
                existStats.setWechatVotes(voteStats.getWechatVotes());
                existStats.setPayVotes(voteStats.getPayVotes());
                existStats.setUniqueVoters(uniqueVoters);
                
                voteStatisticsMapper.updateById(existStats);
            } else {
                // 创建新统计记录
                VoteStatistics statistics = new VoteStatistics();
                statistics.setActivityId(activityId);
                statistics.setStatDate(statDate);
                statistics.setTotalVotes(voteStats.getTotalVotes());
                statistics.setSmsVotes(voteStats.getSmsVotes());
                statistics.setIvrVotes(voteStats.getIvrVotes());
                statistics.setWebVotes(voteStats.getWebVotes());
                statistics.setAppVotes(voteStats.getAppVotes());
                statistics.setWechatVotes(voteStats.getWechatVotes());
                statistics.setPayVotes(voteStats.getPayVotes());
                statistics.setUniqueVoters(uniqueVoters);
                
                voteStatisticsMapper.insert(statistics);
            }

            log.info("生成统计数据完成: activityId={}, date={}, votes={}", 
                    activityId, statDate, voteStats.getTotalVotes());

        } catch (Exception e) {
            log.error("生成统计数据失败: activityId={}, date={}", activityId, statDate, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateBatchStatistics(Long activityId, LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            generateDailyStatistics(activityId, currentDate);
            currentDate = currentDate.plusDays(1);
        }
    }

    @Override
    public ActivityStatisticsDTO getActivityStatistics(Long activityId, LocalDate startDate, LocalDate endDate) {
        // 获取活动信息
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            return null;
        }

        // 获取统计数据
        List<VoteStatistics> statistics = getDailyStatistics(activityId, startDate, endDate);
        
        // 计算汇总数据
        Long totalVotes = statistics.stream().mapToLong(VoteStatistics::getTotalVotes).sum();
        Integer totalUniqueVoters = statistics.stream().mapToInt(VoteStatistics::getUniqueVoters).sum();

        // 各渠道票数汇总
        Map<String, Long> channelVotes = new HashMap<>();
        channelVotes.put("SMS", statistics.stream().mapToLong(VoteStatistics::getSmsVotes).sum());
        channelVotes.put("IVR", statistics.stream().mapToLong(VoteStatistics::getIvrVotes).sum());
        channelVotes.put("WEB", statistics.stream().mapToLong(VoteStatistics::getWebVotes).sum());
        channelVotes.put("APP", statistics.stream().mapToLong(VoteStatistics::getAppVotes).sum());
        channelVotes.put("WECHAT", statistics.stream().mapToLong(VoteStatistics::getWechatVotes).sum());
        channelVotes.put("PAY", statistics.stream().mapToLong(VoteStatistics::getPayVotes).sum());

        // 每日统计
        List<ActivityStatisticsDTO.DailyStatisticsDTO> dailyStats = statistics.stream()
                .map(stat -> ActivityStatisticsDTO.DailyStatisticsDTO.builder()
                        .date(stat.getStatDate())
                        .totalVotes(stat.getTotalVotes())
                        .uniqueVoters(stat.getUniqueVoters())
                        .channelVotes(Map.of(
                                "SMS", stat.getSmsVotes(),
                                "IVR", stat.getIvrVotes(),
                                "WEB", stat.getWebVotes(),
                                "APP", stat.getAppVotes(),
                                "WECHAT", stat.getWechatVotes(),
                                "PAY", stat.getPayVotes()
                        ))
                        .build())
                .collect(Collectors.toList());

        // TOP候选人
        List<ActivityStatisticsDTO.CandidateRankingDTO> topCandidates = getTopCandidates(activityId, 10);

        // 投票趋势
        List<ActivityStatisticsDTO.TrendDataDTO> voteTrend = getVoteTrend(activityId, 7);

        return ActivityStatisticsDTO.builder()
                .activityId(activityId)
                .activityTitle(activity.getTitle())
                .activityStatus(activity.getStatus())
                .startDate(startDate)
                .endDate(endDate)
                .totalVotes(totalVotes)
                .channelVotes(channelVotes)
                .totalUniqueVoters(totalUniqueVoters)
                .dailyStats(dailyStats)
                .topCandidates(topCandidates)
                .voteTrend(voteTrend)
                .build();
    }

    @Override
    public List<VoteStatistics> getDailyStatistics(Long activityId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<VoteStatistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VoteStatistics::getActivityId, activityId)
                   .between(VoteStatistics::getStatDate, startDate, endDate)
                   .orderByAsc(VoteStatistics::getStatDate);

        return voteStatisticsMapper.selectList(queryWrapper);
    }

    @Override
    public List<ActivityStatisticsDTO.CandidateRankingDTO> getTopCandidates(Long activityId, Integer topN) {
        LambdaQueryWrapper<Candidate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Candidate::getActivityId, activityId)
                   .eq(Candidate::getStatus, 0)
                   .orderByDesc(Candidate::getTotalVotes)
                   .last("LIMIT " + topN);

        List<Candidate> candidates = candidateMapper.selectList(queryWrapper);

        return candidates.stream()
                .map(candidate -> ActivityStatisticsDTO.CandidateRankingDTO.builder()
                        .candidateId(candidate.getId())
                        .candidateName(candidate.getName())
                        .groupName(candidate.getGroupName())
                        .totalVotes(candidate.getTotalVotes())
                        .ranking(candidate.getRanking())
                        .avatar(candidate.getAvatar())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityStatisticsDTO.TrendDataDTO> getVoteTrend(Long activityId, Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<VoteStatistics> statistics = getDailyStatistics(activityId, startDate, endDate);

        return statistics.stream()
                .map(stat -> ActivityStatisticsDTO.TrendDataDTO.builder()
                        .date(stat.getStatDate())
                        .votes(stat.getTotalVotes())
                        .uniqueVoters(stat.getUniqueVoters())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ActivityStatisticsDTO getRealTimeStatistics(Long activityId) {
        LocalDate today = LocalDate.now();
        return getActivityStatistics(activityId, today, today);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredStatistics(Integer days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        
        LambdaQueryWrapper<VoteStatistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(VoteStatistics::getStatDate, cutoffDate);
        
        int deleted = voteStatisticsMapper.delete(queryWrapper);
        log.info("清理过期统计数据: 删除{}条记录", deleted);
    }
}