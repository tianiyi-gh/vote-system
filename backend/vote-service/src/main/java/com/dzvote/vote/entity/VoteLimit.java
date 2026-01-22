package com.dzvote.vote.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投票限制实体
 */
@Data
public class VoteLimit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 投票人手机号
     */
    private String voterPhone;

    /**
     * 投票人IP
     */
    private String voterIp;

    /**
     * 投票次数
     */
    private Integer voteCount;

    /**
     * 最后投票时间
     */
    private LocalDateTime lastVoteTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}