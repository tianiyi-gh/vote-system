package com.dzvote.common.service;

import java.awt.image.BufferedImage;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 生成验证码
     * @param key 验证码key
     * @return 验证码图片
     */
    BufferedImage generateCaptcha(String key);

    /**
     * 生成验证码文本
     * @param key 验证码key
     * @return 验证码文本
     */
    String generateCaptchaText(String key);

    /**
     * 验证验证码
     * @param key 验证码key
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean validateCaptcha(String key, String code);

    /**
     * 移除验证码
     * @param key 验证码key
     */
    void removeCaptcha(String key);
}