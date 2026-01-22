package com.dzvote.vote.service;

import java.util.Map;

/**
 * 短信服务接口
 */
public interface SmsService {

    /**
     * 发送验证码短信
     * @param phoneNumber 手机号
     * @param code 验证码
     * @return 发送结果
     */
    boolean sendVerificationCode(String phoneNumber, String code);

    /**
     * 发送投票通知短信
     * @param phoneNumber 手机号
     * @param templateParams 模板参数
     * @return 发送结果
     */
    boolean sendVoteNotification(String phoneNumber, Map<String, Object> templateParams);

    /**
     * 发送活动提醒短信
     * @param phoneNumber 手机号
     * @param activityTitle 活动标题
     * @param endTime 结束时间
     * @return 发送结果
     */
    boolean sendActivityReminder(String phoneNumber, String activityTitle, String endTime);
}
