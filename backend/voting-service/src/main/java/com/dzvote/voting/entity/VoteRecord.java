package com.dzvote.voting.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 投票记录实体类
 */
@Data
public class VoteRecord {
    
    private Long id;
    private Long activityId;
    private Long candidateId;
    private String voterName;
    private String voterPhone;
    private String voterIp;
    private String channel;
    private LocalDateTime voteTime;
    private Integer valid;
    private String deviceInfo;
    private String location;
}
