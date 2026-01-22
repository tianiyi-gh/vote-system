package com.dzvote.user.service.impl;

import com.dzvote.user.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int CAPTCHA_LENGTH = 6;

    @Override
    public String generateCaptcha(String phone) {
        // 生成6位数字验证码
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        String captchaCode = code.toString();

        // 存储到Redis，5分钟过期
        String key = CAPTCHA_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(key, captchaCode, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("验证码生成成功: phone={}, code={}", phone, captchaCode);
        return captchaCode;
    }

    @Override
    public boolean verifyCaptcha(String phone, String code) {
        String key = CAPTCHA_KEY_PREFIX + phone;
        String storedCode = (String) redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("验证码不存在或已过期: phone={}", phone);
            return false;
        }

        boolean valid = storedCode.equals(code);
        if (valid) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
            log.info("验证码验证成功: phone={}", phone);
        } else {
            log.warn("验证码验证失败: phone={}, inputCode={}, storedCode={}", phone, code, storedCode);
        }

        return valid;
    }
}
