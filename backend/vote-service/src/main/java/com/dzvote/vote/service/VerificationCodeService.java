package com.dzvote.vote.service;

import com.dzvote.vote.dto.VerifyCodeRequest;

/**
 * 验证码服务
 */
public interface VerificationCodeService {

    /**
     * 发送验证码
     * @param type 类型：SMS/EMAIL
     * @param target 目标：手机号/邮箱
     * @param purpose 用途：VOTE/LOGIN/REGISTER
     * @param ipAddress IP地址
     * @param deviceFingerprint 设备指纹
     * @return 发送结果
     */
    boolean sendCode(String type, String target, String purpose, String ipAddress, String deviceFingerprint);

    /**
     * 验证验证码
     * @param request 验证请求
     * @return 验证结果
     */
    boolean verifyCode(VerifyCodeRequest request);

    /**
     * 检查IP是否频繁发送验证码
     * @param ipAddress IP地址
     * @return true-频繁 false-正常
     */
    boolean isFrequentSender(String ipAddress);
}
