package com.dzvote.common.service.impl;

import com.dzvote.common.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final int EXPIRE_MINUTES = 5;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;

    @Override
    public BufferedImage generateCaptcha(String key) {
        String code = generateCaptchaText(key);
        return createCaptchaImage(code);
    }

    @Override
    public String generateCaptchaText(String key) {
        String code = generateRandomCode();
        
        // 保存到Redis，5分钟过期
        String redisKey = CAPTCHA_KEY_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, code.toLowerCase(), EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        log.debug("生成验证码: key={}, code={}", key, code);
        return code;
    }

    @Override
    public boolean validateCaptcha(String key, String code) {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(code)) {
            return false;
        }
        
        String redisKey = CAPTCHA_KEY_PREFIX + key;
        String savedCode = (String) redisTemplate.opsForValue().get(redisKey);
        
        boolean valid = savedCode != null && savedCode.equalsIgnoreCase(code);
        
        if (valid) {
            // 验证成功后删除验证码
            removeCaptcha(key);
        }
        
        log.debug("验证验证码: key={}, code={}, savedCode={}, valid={}", key, code, savedCode, valid);
        return valid;
    }

    @Override
    public void removeCaptcha(String key) {
        String redisKey = CAPTCHA_KEY_PREFIX + key;
        redisTemplate.delete(redisKey);
    }

    /**
     * 生成随机验证码
     */
    private String generateRandomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除易混淆字符
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }

    /**
     * 创建验证码图片
     */
    private BufferedImage createCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 填充背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 添加干扰线
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g2d.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), 
                        random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }
        
        // 添加噪点
        for (int i = 0; i < 100; i++) {
            g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g2d.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 1, 1);
        }
        
        // 绘制验证码
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            g2d.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
            
            // 随机旋转角度
            double angle = (random.nextDouble() - 0.5) * 0.4;
            
            // 计算字符位置
            int x = 20 + i * 25;
            int y = 25 + random.nextInt(10);
            
            // 旋转和绘制
            g2d.rotate(angle, x, y);
            g2d.drawString(String.valueOf(code.charAt(i)), x, y);
            g2d.rotate(-angle, x, y);
        }
        
        g2d.dispose();
        return image;
    }
}