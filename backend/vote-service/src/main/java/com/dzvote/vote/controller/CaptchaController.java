package com.dzvote.vote.controller;

import com.dzvote.vote.util.CaptchaUtil;
import com.dzvote.vote.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 生成验证码
     */
    @GetMapping
    public ResponseEntity<Result<String>> generateCaptcha() {
        try {
            // 生成唯一key
            String key = UUID.randomUUID().toString().substring(0, 8);

            // 生成验证码并存储
            String captchaData = CaptchaUtil.generateCaptchaWithKey(key, redisTemplate, 5);

            // 返回Base64格式的图片数据
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.success(captchaData));
        } catch (Exception e) {
            return ResponseEntity.ok()
                    .body(Result.error("生成验证码失败: " + e.getMessage()));
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify")
    public Result<Boolean> verifyCaptcha(@RequestBody VerifyRequest request) {
        try {
            boolean isValid = CaptchaUtil.verifyCaptcha(request.getKey(), request.getCode(), redisTemplate);
            return Result.success(isValid);
        } catch (Exception e) {
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证请求对象
     */
    public static class VerifyRequest {
        private String key;
        private String code;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
