package com.dzvote.voting.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 投票限制实体类
 */
@Data
public class VoteLimit {
    
    private Long id;
    private Long activityId;
    private String voterPhone;
    private String voterIp;
    private Integer voteCount;
    private LocalDateTime lastVoteTime;
    private LocalDateTime createdAt;
}
