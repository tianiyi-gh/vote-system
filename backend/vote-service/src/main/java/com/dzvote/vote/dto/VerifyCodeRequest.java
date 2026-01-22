package com.dzvote.vote.dto;

import lombok.Data;

/**
 * 验证码验证请求
 */
@Data
public class VerifyCodeRequest {

    /**
     * 验证码ID
     */
    private Long codeId;

    /**
     * 验证码
     */
    private String code;

    /**
     * 接收目标：手机号/邮箱
     */
    private String target;

    /**
     * 用途：VOTE/LOGIN/REGISTER
     */
    private String purpose;
}
