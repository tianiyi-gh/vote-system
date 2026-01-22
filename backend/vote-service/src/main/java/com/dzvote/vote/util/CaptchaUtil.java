package com.dzvote.vote.util;

import org.springframework.data.redis.core.RedisTemplate;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * 验证码工具类
 */
public class CaptchaUtil {

    private static final String CAPTCHA_PREFIX = "captcha:";

    /**
     * 生成验证码
     * @param width 宽度
     * @param height 高度
     * @param codeLength 验证码长度
     * @return 验证码图片的Base64字符串
     */
    public static String generateCaptcha(int width, int height, int codeLength) {
        // 创建图片缓冲区
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        // 设置抗锯齿
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 填充背景色
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        // 生成随机验证码
        String code = generateRandomCode(codeLength);
        char[] codeChars = code.toCharArray();

        Random random = new Random();

        // 绘制干扰线
        for (int i = 0; i < 7; i++) {
            graphics.setColor(getRandomColor(150, 250));
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            graphics.drawLine(x, y, x1, y1);
        }

        // 绘制干扰点
        for (int i = 0; i < 50; i++) {
            graphics.setColor(getRandomColor(150, 250));
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            graphics.fillOval(x, y, 1, 1);
        }

        // 绘制验证码
        for (int i = 0; i < codeChars.length; i++) {
            // 设置随机颜色
            graphics.setColor(getRandomColor(50, 150));

            // 设置随机字体
            String[] fonts = {"Arial", "Verdana", "Georgia"};
            String fontName = fonts[random.nextInt(fonts.length)];
            int fontSize = random.nextInt(10) + 25; // 25-35
            int fontStyle = random.nextInt(3);
            graphics.setFont(new Font(fontName, fontStyle, fontSize));

            // 旋转角度
            double angle = random.nextDouble() * 0.3 - 0.15; // -0.15 到 0.15 弧度

            // 计算位置
            int x = (width / (codeChars.length + 1)) * (i + 1);
            int y = height / 2 + fontSize / 3;

            // 绘制旋转文字
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.rotate(angle, x, y);
            g2d.drawString(String.valueOf(codeChars[i]), x - fontSize / 4, y);
            g2d.rotate(-angle, x, y);
        }

        graphics.dispose();

        // 转换为Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("生成验证码失败", e);
        }
    }

    /**
     * 生成随机验证码
     */
    private static String generateRandomCode(int length) {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * 获取随机颜色
     */
    private static Color getRandomColor(int min, int max) {
        Random random = new Random();
        int r = random.nextInt(max - min) + min;
        int g = random.nextInt(max - min) + min;
        int b = random.nextInt(max - min) + min;
        return new Color(r, g, b);
    }

    /**
     * 生成验证码并存储到Redis
     * @param key 验证码key
     * @param redisTemplate Redis模板
     * @param expireMinutes 过期时间（分钟）
     * @return 验证码图片的Base64字符串
     */
    public static String generateCaptchaWithKey(String key, RedisTemplate<String, String> redisTemplate, int expireMinutes) {
        String code = generateRandomCode(4);
        String captcha = CaptchaUtil.generateCaptcha(120, 40, code.length());

        // 存储到Redis
        String redisKey = CAPTCHA_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, code, expireMinutes, TimeUnit.MINUTES);

        // 返回包含key和图片的数据
        return String.format("%s,%s", key, captcha);
    }

    /**
     * 验证验证码
     * @param key 验证码key
     * @param code 用户输入的验证码
     * @param redisTemplate Redis模板
     * @return 是否验证成功
     */
    public static boolean verifyCaptcha(String key, String code, RedisTemplate<String, String> redisTemplate) {
        if (key == null || code == null) {
            return false;
        }

        String redisKey = CAPTCHA_PREFIX + key;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            return false;
        }

        // 验证后删除验证码
        redisTemplate.delete(redisKey);

        return storedCode.equalsIgnoreCase(code);
    }
}
