package com.dzvote.voting.service.impl;

import com.dzvote.voting.dto.VoteRequest;
import com.dzvote.voting.dto.VoteResult;
import com.dzvote.voting.entity.VoteRecord;
import com.dzvote.voting.entity.VoteLimit;
import com.dzvote.voting.mapper.VoteRecordMapper;
import com.dzvote.voting.mapper.VoteLimitMapper;
import com.dzvote.voting.mapper.CandidateMapper;
import com.dzvote.voting.service.VotingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 投票服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VotingServiceImpl implements VotingService {
    
    private final VoteRecordMapper voteRecordMapper;
    private final VoteLimitMapper voteLimitMapper;
    private final CandidateMapper candidateMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public VoteResult vote(VoteRequest request) {
        try {
            // 1. 检查投票限制
            if (!checkVoteLimit(request.getActivityId(), request.getVoterPhone())) {
                return VoteResult.failure("今日投票次数已达上限");
            }
            
            // 2. 创建投票记录
            VoteRecord record = new VoteRecord();
            record.setActivityId(request.getActivityId());
            record.setCandidateId(request.getCandidateId());
            record.setVoterName(request.getVoterName());
            record.setVoterPhone(request.getVoterPhone());
            record.setChannel(request.getChannel());
            record.setVoteTime(LocalDateTime.now());
            record.setValid(1);
            
            int result = voteRecordMapper.insert(record);
            if (result <= 0) {
                return VoteResult.failure("投票记录创建失败");
            }
            
            // 3. 更新候选人票数
            candidateMapper.incrementVotes(request.getCandidateId());
            
            // 4. 更新投票限制
            updateVoteLimit(request.getActivityId(), request.getVoterPhone());
            
            // 5. 更新活动总票数
            updateActivityVotes(request.getActivityId());
            
            // 6. 更新缓存
            String cacheKey = "candidate:" + request.getCandidateId() + ":votes";
            Integer currentVotes = candidateMapper.selectVotesById(request.getCandidateId());
            redisTemplate.opsForValue().set(cacheKey, currentVotes);
            
            // 7. 获取剩余投票次数
            Integer remaining = getRemainingVotes(request.getActivityId(), request.getVoterPhone());
            
            log.info("投票成功: 活动ID={}, 候选人ID={}, 手机号={}", 
                request.getActivityId(), request.getCandidateId(), request.getVoterPhone());
            
            return VoteResult.success(record.getId(), remaining, currentVotes);
            
        } catch (Exception e) {
            log.error("投票失败", e);
            throw new RuntimeException("投票失败", e);
        }
    }
    
    @Override
    public boolean checkVoteLimit(Long activityId, String phone) {
        // 先从缓存检查
        String cacheKey = "vote:limit:" + activityId + ":" + phone;
        Integer count = (Integer) redisTemplate.opsForValue().get(cacheKey);
        
        if (count != null) {
            return count < 10; // 每日最多10票
        }
        
        // 缓存未命中，查询数据库
        VoteLimit limit = voteLimitMapper.selectByActivityAndPhone(activityId, phone);
        if (limit != null) {
            redisTemplate.opsForValue().set(cacheKey, limit.getVoteCount());
            return limit.getVoteCount() < 10;
        }
        
        return true;
    }
    
    /**
     * 更新投票限制
     */
    private void updateVoteLimit(Long activityId, String phone) {
        VoteLimit limit = voteLimitMapper.selectByActivityAndPhone(activityId, phone);
        
        if (limit == null) {
            limit = new VoteLimit();
            limit.setActivityId(activityId);
            limit.setVoterPhone(phone);
            limit.setVoteCount(1);
            limit.setLastVoteTime(LocalDateTime.now());
            limit.setCreatedAt(LocalDateTime.now());
            voteLimitMapper.insert(limit);
        } else {
            limit.setVoteCount(limit.getVoteCount() + 1);
            limit.setLastVoteTime(LocalDateTime.now());
            voteLimitMapper.updateById(limit);
        }
        
        // 更新缓存
        String cacheKey = "vote:limit:" + activityId + ":" + phone;
        redisTemplate.opsForValue().set(cacheKey, limit.getVoteCount());
    }
    
    /**
     * 获取剩余投票次数
     */
    private Integer getRemainingVotes(Long activityId, String phone) {
        String cacheKey = "vote:limit:" + activityId + ":" + phone;
        Integer count = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (count == null) {
            return 10;
        }
        return 10 - count;
    }
    
    /**
     * 更新活动总票数
     */
    private void updateActivityVotes(Long activityId) {
        // 可以通过消息队列异步更新
        String cacheKey = "activity:" + activityId + ":total_votes";
        redisTemplate.opsForValue().increment(cacheKey);
    }
}
