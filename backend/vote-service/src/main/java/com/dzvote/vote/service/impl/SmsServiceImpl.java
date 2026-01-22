package com.dzvote.vote.service.impl;

import com.dzvote.vote.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 短信服务实现（模拟版本，实际使用时需接入阿里云/腾讯云等短信服务）
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    /**
     * 模拟发送验证码短信
     */
    @Override
    public boolean sendVerificationCode(String phoneNumber, String code) {
        try {
            // TODO: 实际使用时需接入短信服务商
            // 示例：阿里云短信服务
            // DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
            // IAcsClient client = new DefaultAcsClient(profile);
            // SendSmsRequest request = new SendSmsRequest();
            // request.setPhoneNumbers(phoneNumber);
            // request.setSignName("投票系统");
            // request.setTemplateCode("SMS_xxxxxxxx");
            // request.setTemplateParam("{\"code\":\"" + code + "\"}");
            // SendSmsResponse response = client.getAcsResponse(request);
            // return "OK".equals(response.getCode());

            log.info("【模拟短信】发送验证码到 {}: 验证码为 {}，有效时间5分钟", phoneNumber, code);
            return true;
        } catch (Exception e) {
            log.error("发送短信验证码失败", e);
            return false;
        }
    }

    /**
     * 发送投票通知短信
     */
    @Override
    public boolean sendVoteNotification(String phoneNumber, Map<String, Object> templateParams) {
        try {
            log.info("【模拟短信】发送投票通知到 {}: {}", phoneNumber, templateParams);
            return true;
        } catch (Exception e) {
            log.error("发送投票通知失败", e);
            return false;
        }
    }

    /**
     * 发送活动提醒短信
     */
    @Override
    public boolean sendActivityReminder(String phoneNumber, String activityTitle, String endTime) {
        try {
            log.info("【模拟短信】发送活动提醒到 {}: 活动【{}】将于 {} 结束", phoneNumber, activityTitle, endTime);
            return true;
        } catch (Exception e) {
            log.error("发送活动提醒失败", e);
            return false;
        }
    }
}
