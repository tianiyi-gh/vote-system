package com.dzvote.gateway.service.impl;

import com.dzvote.gateway.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    @Value("${sms.api.url:}")
    private String smsApiUrl;

    @Value("${sms.api.appid:}")
    private String smsAppId;

    @Value("${sms.api.appkey:}")
    private String smsAppKey;

    private static final String SMS_CODE_PREFIX = "sms_code:";
    private static final int EXPIRE_MINUTES = 5;

    @Override
    public boolean sendVoteCode(String phone, String code) {
        try {
            if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
                log.warn("手机号或验证码为空");
                return false;
            }

            // 保存验证码到Redis
            String key = SMS_CODE_PREFIX + phone;
            redisTemplate.opsForValue().set(key, code, EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 调用短信API发送验证码
            boolean result = callSmsApi(phone, buildVoteCodeMessage(code));
            
            log.info("发送投票验证码: phone={}, code={}, result={}", phone, code, result);
            return result;

        } catch (Exception e) {
            log.error("发送投票验证码失败: phone={}", phone, e);
            return false;
        }
    }

    @Override
    public boolean sendVoteConfirm(String phone, String candidateName) {
        try {
            String message = buildVoteConfirmMessage(candidateName);
            boolean result = callSmsApi(phone, message);
            
            log.info("发送投票确认短信: phone={}, candidate={}, result={}", phone, candidateName, result);
            return result;

        } catch (Exception e) {
            log.error("发送投票确认短信失败: phone={}, candidate={}", phone, candidateName, e);
            return false;
        }
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        try {
            if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
                return false;
            }

            String key = SMS_CODE_PREFIX + phone;
            String savedCode = (String) redisTemplate.opsForValue().get(key);
            
            boolean valid = savedCode != null && savedCode.equals(code);
            
            if (valid) {
                // 验证成功后删除验证码
                redisTemplate.delete(key);
            }
            
            log.debug("验证短信验证码: phone={}, code={}, savedCode={}, valid={}", phone, code, savedCode, valid);
            return valid;

        } catch (Exception e) {
            log.error("验证短信验证码失败", e);
            return false;
        }
    }

    /**
     * 调用短信API
     */
    private boolean callSmsApi(String phone, String message) {
        try {
            // 这里使用模拟的实现，实际应该调用真实的短信服务商API
            Map<String, Object> params = new HashMap<>();
            params.put("appid", smsAppId);
            params.put("appkey", smsAppKey);
            params.put("phone", phone);
            params.put("message", message);

            ResponseEntity<Map> response = restTemplate.postForEntity(smsApiUrl, params, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                if (body != null && "0".equals(body.get("code"))) {
                    return true;
                }
            }

            log.warn("短信API调用失败: {}", response.getBody());
            return false;

        } catch (Exception e) {
            log.error("调用短信API失败", e);
            return false;
        }
    }

    /**
     * 构建投票验证码消息
     */
    private String buildVoteCodeMessage(String code) {
        return String.format("【DZVOTE】您的投票验证码是：%s，5分钟内有效。", code);
    }

    /**
     * 构建投票确认消息
     */
    private String buildVoteConfirmMessage(String candidateName) {
        return String.format("【DZVOTE】您已成功为%s投票，感谢您的参与！", candidateName);
    }
}