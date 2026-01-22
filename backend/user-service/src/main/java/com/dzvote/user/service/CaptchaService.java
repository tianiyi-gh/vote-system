package com.dzvote.user.service;

public interface CaptchaService {

    /**
     * 生成验证码
     */
    String generateCaptcha(String phone);

    /**
     * 验证验证码
     */
    boolean verifyCaptcha(String phone, String code);
}
