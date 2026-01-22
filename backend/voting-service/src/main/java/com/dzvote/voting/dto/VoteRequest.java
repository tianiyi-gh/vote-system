package com.dzvote.voting.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 投票请求
 */
@Data
public class VoteRequest {
    
    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
    
    /**
     * 候选人ID
     */
    @NotNull(message = "候选人ID不能为空")
    private Long candidateId;
    
    /**
     * 投票人姓名
     */
    private String voterName;
    
    /**
     * 投票人手机号
     */
    private String voterPhone;
    
    /**
     * 投票渠道 (SMS/IVR/WEB/APP/WECHAT/PAY)
     */
    private String channel;
}
