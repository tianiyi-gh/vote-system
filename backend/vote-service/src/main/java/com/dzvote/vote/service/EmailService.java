package com.dzvote.vote.service;

import java.util.Map;

/**
 * 邮箱服务接口
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     * @param emailAddress 邮箱地址
     * @param code 验证码
     * @return 发送结果
     */
    boolean sendVerificationCode(String emailAddress, String code);

    /**
     * 发送投票通知邮件
     * @param emailAddress 邮箱地址
     * @param templateParams 模板参数
     * @return 发送结果
     */
    boolean sendVoteNotification(String emailAddress, Map<String, Object> templateParams);

    /**
     * 发送活动提醒邮件
     * @param emailAddress 邮箱地址
     * @param activityTitle 活动标题
     * @param endTime 结束时间
     * @return 发送结果
     */
    boolean sendActivityReminder(String emailAddress, String activityTitle, String endTime);
}
