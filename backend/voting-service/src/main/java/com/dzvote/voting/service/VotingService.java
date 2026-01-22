package com.dzvote.voting.service;

import com.dzvote.voting.dto.VoteRequest;
import com.dzvote.voting.dto.VoteResult;

/**
 * 投票服务接口
 */
public interface VotingService {
    
    /**
     * 投票
     */
    VoteResult vote(VoteRequest request);
    
    /**
     * 检查投票限制
     */
    boolean checkVoteLimit(Long activityId, String phone);
}
