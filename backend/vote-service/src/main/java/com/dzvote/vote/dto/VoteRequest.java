package com.dzvote.vote.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 投票请求DTO
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
     * 投票渠道：SMS/IVR/WEB/APP/WECHAT/PAY
     */
    @NotBlank(message = "投票渠道不能为空")
    @Pattern(regexp = "^(SMS|IVR|WEB|APP|WECHAT|PAY)$", message = "投票渠道无效")
    private String channel;

    /**
     * 投票人手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String voterPhone;

    /**
     * 投票人IP
     */
    private String voterIp;

    /**
     * 投票人标识
     */
    private String voterId;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码key
     */
    private String captchaKey;

    /**
     * 投票数量（用于批量投票）
     */
    private Integer voteCount = 1;
}