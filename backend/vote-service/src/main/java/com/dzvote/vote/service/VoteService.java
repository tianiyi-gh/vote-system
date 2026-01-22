package com.dzvote.vote.service;

import com.dzvote.vote.entity.VoteRecord;
import com.dzvote.vote.dto.VoteRequest;
import com.dzvote.vote.dto.VoteResult;

/**
 * 投票服务接口
 */
public interface VoteService {

    /**
     * 进行投票
     * @param voteRequest 投票请求
     * @return 投票结果
     */
    VoteResult vote(VoteRequest voteRequest);

    /**
     * 检查投票限制
     * @param voteRequest 投票请求
     * @return 是否允许投票
     */
    boolean checkVoteLimit(VoteRequest voteRequest);

    /**
     * 验证投票有效性
     * @param voteRequest 投票请求
     * @return 验证结果
     */
    boolean validateVote(VoteRequest voteRequest);
}