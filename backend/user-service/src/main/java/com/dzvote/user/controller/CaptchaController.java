package com.dzvote.user.controller;

import com.dzvote.user.util.Result;
import com.dzvote.user.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 生成验证码
     */
    @GetMapping("/generate")
    public Result<Map<String, String>> generate(@RequestParam String phone) {
        try {
            String captchaCode = captchaService.generateCaptcha(phone);
            return Result.success(Map.of(
                "message", "验证码已生成",
                "captcha", captchaCode
            ));
        } catch (Exception e) {
            return Result.error("生成验证码失败: " + e.getMessage());
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify")
    public Result<Boolean> verify(@RequestParam String phone,
                                   @RequestParam String code) {
        boolean valid = captchaService.verifyCaptcha(phone, code);
        if (valid) {
            return Result.success(true);
        } else {
            return Result.error("验证码错误或已过期");
        }
    }
}
