package com.dzvote.voting.dto;

import lombok.Data;

/**
 * 投票结果
 */
@Data
public class VoteResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 投票记录ID
     */
    private Long recordId;
    
    /**
     * 剩余投票次数
     */
    private Integer remainingVotes;
    
    /**
     * 候选人当前票数
     */
    private Integer currentVotes;
    
    public static VoteResult success(Long recordId, Integer remainingVotes, Integer currentVotes) {
        VoteResult result = new VoteResult();
        result.setSuccess(true);
        result.setMessage("投票成功");
        result.setRecordId(recordId);
        result.setRemainingVotes(remainingVotes);
        result.setCurrentVotes(currentVotes);
        return result;
    }
    
    public static VoteResult failure(String message) {
        VoteResult result = new VoteResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}
