package com.dzvote.gateway.service;

/**
 * 短信服务接口
 */
public interface SmsService {

    /**
     * 发送投票验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 发送结果
     */
    boolean sendVoteCode(String phone, String code);

    /**
     * 发送投票确认短信
     * @param phone 手机号
     * @param candidateName 候选人姓名
     * @return 发送结果
     */
    boolean sendVoteConfirm(String phone, String candidateName);

    /**
     * 验证短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String phone, String code);
}