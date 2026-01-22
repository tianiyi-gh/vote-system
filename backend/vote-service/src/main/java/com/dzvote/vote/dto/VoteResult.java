package com.dzvote.vote.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 投票结果DTO
 */
@Data
@Builder
public class VoteResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 投票记录ID
     */
    private Long voteId;

    /**
     * 候选人当前总票数
     */
    private Long totalVotes;

    /**
     * 投票时间
     */
    private LocalDateTime voteTime;

    /**
     * 投票权重
     */
    private Integer weight;

    /**
     * 有效票数
     */
    private Integer validVotes;

    /**
     * 无效票数
     */
    private Integer invalidVotes;

    /**
     * 创建成功结果
     */
    public static VoteResult success(Long voteId, Long totalVotes) {
        return VoteResult.builder()
                .success(true)
                .voteId(voteId)
                .totalVotes(totalVotes)
                .voteTime(LocalDateTime.now())
                .validVotes(1)
                .invalidVotes(0)
                .build();
    }

    /**
     * 创建成功结果（简化版）
     */
    public static VoteResult success(Long voteId) {
        return VoteResult.builder()
                .success(true)
                .voteId(voteId)
                .voteTime(LocalDateTime.now())
                .validVotes(1)
                .invalidVotes(0)
                .build();
    }

    /**
     * 获取数据（投票ID）
     */
    public Long getData() {
        return this.voteId;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(this.success);
    }

    /**
     * 创建失败结果
     */
    public static VoteResult failure(String errorCode, String errorMessage) {
        return VoteResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .validVotes(0)
                .invalidVotes(1)
                .build();
    }

    /**
     * 创建批量投票结果
     */
    public static VoteResult batchResult(Integer validVotes, Integer invalidVotes) {
        return VoteResult.builder()
                .success(validVotes > 0)
                .validVotes(validVotes)
                .invalidVotes(invalidVotes)
                .voteTime(LocalDateTime.now())
                .build();
    }
}