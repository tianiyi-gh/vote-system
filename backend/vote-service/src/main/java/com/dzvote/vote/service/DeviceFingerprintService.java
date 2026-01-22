package com.dzvote.vote.service;

/**
 * 设备指纹服务
 */
public interface DeviceFingerprintService {

    /**
     * 生成设备指纹
     * @param userAgent User-Agent
     * @param acceptLanguage Accept-Language
     * @param screenResolution 屏幕分辨率
     * @param timezone 时区
     * @return 设备指纹
     */
    String generateFingerprint(String userAgent, String acceptLanguage, String screenResolution, String timezone);

    /**
     * 检查设备指纹是否异常
     * @param fingerprint 设备指纹
     * @return true-异常 false-正常
     */
    boolean isSuspicious(String fingerprint);
}
