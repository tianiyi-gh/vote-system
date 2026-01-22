package com.dzvote.vote.service.impl;

import com.dzvote.vote.service.DeviceFingerprintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备指纹服务实现
 */
@Slf4j
@Service
public class DeviceFingerprintServiceImpl implements DeviceFingerprintService {

    /**
     * 设备指纹缓存（记录设备使用情况）
     */
    private final Map<String, DeviceInfo> fingerprintCache = new ConcurrentHashMap<>();

    /**
     * 生成设备指纹
     */
    @Override
    public String generateFingerprint(String userAgent, String acceptLanguage, String screenResolution, String timezone) {
        try {
            // 组合设备特征
            String fingerprintData = userAgent + "|" + acceptLanguage + "|" + screenResolution + "|" + timezone;

            // 使用SHA256生成指纹
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fingerprintData.getBytes("UTF-8"));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // 取前16位作为指纹
            String fingerprint = hexString.toString().substring(0, 16);

            // 记录设备信息
            DeviceInfo deviceInfo = fingerprintCache.getOrDefault(fingerprint, new DeviceInfo());
            deviceInfo.fingerprint = fingerprint;
            deviceInfo.userAgent = userAgent;
            deviceInfo.lastSeen = System.currentTimeMillis();
            deviceInfo.voteCount++;
            fingerprintCache.put(fingerprint, deviceInfo);

            log.debug("生成设备指纹: {}", fingerprint);
            return fingerprint;

        } catch (Exception e) {
            log.error("生成设备指纹异常", e);
            return null;
        }
    }

    /**
     * 检查设备指纹是否异常
     */
    @Override
    public boolean isSuspicious(String fingerprint) {
        try {
            if (fingerprint == null || fingerprint.isEmpty()) {
                return true;
            }

            DeviceInfo deviceInfo = fingerprintCache.get(fingerprint);
            if (deviceInfo == null) {
                return false;
            }

            // 检查设备投票频率（1分钟内超过10次视为异常）
            long currentTime = System.currentTimeMillis();
            if (currentTime - deviceInfo.lastSeen < 60000 && deviceInfo.voteCount > 10) {
                log.warn("设备投票频率异常: Fingerprint={}, Count={}", fingerprint, deviceInfo.voteCount);
                return true;
            }

            // 检查是否为已知的机器人/爬虫User-Agent
            if (deviceInfo.userAgent != null) {
                String ua = deviceInfo.userAgent.toLowerCase();
                if (ua.contains("bot") || ua.contains("spider") || ua.contains("crawler") ||
                    ua.contains("curl") || ua.contains("wget") || ua.contains("python")) {
                    log.warn("设备User-Agent异常: Fingerprint={}, UserAgent={}", fingerprint, deviceInfo.userAgent);
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("检查设备指纹异常", e);
            return false;
        }
    }

    /**
     * 获取设备使用统计
     */
    public List<DeviceStats> getDeviceStats() {
        List<DeviceStats> stats = new ArrayList<>();
        for (Map.Entry<String, DeviceInfo> entry : fingerprintCache.entrySet()) {
            DeviceStats stat = new DeviceStats();
            stat.fingerprint = entry.getKey();
            stat.voteCount = entry.getValue().voteCount;
            stat.lastSeen = entry.getValue().lastSeen;
            stats.add(stat);
        }
        return stats;
    }

    /**
     * 设备信息
     */
    private static class DeviceInfo {
        String fingerprint;
        String userAgent;
        long lastSeen;
        int voteCount = 0;
    }

    /**
     * 设备统计
     */
    public static class DeviceStats {
        public String fingerprint;
        public int voteCount;
        public long lastSeen;
    }
}
