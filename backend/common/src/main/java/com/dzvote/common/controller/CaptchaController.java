package com.dzvote.common.controller;

import com.dzvote.common.service.CaptchaService;
import com.dzvote.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * 验证码控制器
 */
@Slf4j
@Tag(name = "验证码", description = "验证码相关接口")
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @Operation(summary = "获取验证码")
    @GetMapping
    public Result<String> getCaptcha() {
        try {
            String key = UUID.randomUUID().toString().replace("-", "");
            BufferedImage image = captchaService.generateCaptcha(key);
            
            // 将图片转为Base64（不包含data:image/png;base64前缀）
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            
            // 返回格式：key|base64
            String data = key + "|" + base64;
            
            return Result.success("获取成功", data);
        } catch (IOException e) {
            log.error("生成验证码失败", e);
            return Result.failure("GENERATE_FAILED", "生成验证码失败");
        }
    }

    @Operation(summary = "获取验证码（带key）")
    @GetMapping("/{key}")
    public Result<String> getCaptchaWithKey(@PathVariable String key) {
        try {
            BufferedImage image = captchaService.generateCaptcha(key);
            
            // 将图片转为Base64（不包含data:image/png;base64前缀）
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            
            // 返回格式：key|base64
            String data = key + "|" + base64;
            
            return Result.success("获取成功", data);
        } catch (IOException e) {
            log.error("生成验证码失败: key={}", key, e);
            return Result.failure("GENERATE_FAILED", "生成验证码失败");
        }
    }

    @Operation(summary = "验证验证码")
    @PostMapping("/validate")
    public Result<Boolean> validateCaptcha(
            @RequestParam String key,
            @RequestParam String code) {
        
        boolean valid = captchaService.validateCaptcha(key, code);
        return Result.success(valid ? "验证成功" : "验证失败", valid);
    }
}