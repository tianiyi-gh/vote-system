package com.dzvote.vote.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 投票请求DTO - 简化版
 */
public class VoteDTO {
    
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
    
    @NotNull(message = "候选人ID不能为空")
    private Long candidateId;
    
    @NotBlank(message = "投票渠道不能为空")
    @Pattern(regexp = "^(SMS|IVR|WEB|APP|WECHAT|PAY)$", message = "投票渠道无效")
    private String channel;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String voterPhone;
    
    private String voterIp;
    private String voterName;
    
    // Getter和Setter方法
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    
    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }
    
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    
    public String getVoterPhone() { return voterPhone; }
    public void setVoterPhone(String voterPhone) { this.voterPhone = voterPhone; }
    
    public String getVoterIp() { return voterIp; }
    public void setVoterIp(String voterIp) { this.voterIp = voterIp; }
    
    public String getVoterName() { return voterName; }
    public void setVoterName(String voterName) { this.voterName = voterName; }
}