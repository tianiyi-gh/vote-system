package com.dzvote.vote.controller;

import com.dzvote.vote.dto.VerifyCodeRequest;
import com.dzvote.vote.entity.IpBlacklist;
import com.dzvote.vote.entity.VerificationCode;
import com.dzvote.vote.mapper.VerificationCodeMapper;
import com.dzvote.vote.mapper.VerificationCodeMapper.VerificationCodeStats;
import com.dzvote.vote.service.DeviceFingerprintService;
import com.dzvote.vote.service.FraudDetectionService;
import com.dzvote.vote.service.IpBlacklistService;
import com.dzvote.vote.service.VerificationCodeService;
import com.dzvote.vote.util.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备统计DTO（从DeviceFingerprintServiceImpl中移出，避免内部类访问问题）
 */
class DeviceStatsDto {
    public String fingerprint;
    public int voteCount;
    public long lastSeen;
}

/**
 * 安全防护控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {

    private final VerificationCodeService verificationCodeService;
    private final IpBlacklistService ipBlacklistService;
    private final DeviceFingerprintService deviceFingerprintService;
    private final FraudDetectionService fraudDetectionService;
    private final VerificationCodeMapper verificationCodeMapper;

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public Result<String> sendSmsCode(@RequestParam String phoneNumber,
                                       @RequestParam(required = false) String purpose,
                                       HttpServletRequest request) {
        try {
            String ipAddress = getClientIp(request);
            String deviceFingerprint = request.getHeader("X-Device-Fingerprint");

            boolean success = verificationCodeService.sendCode("SMS", phoneNumber,
                purpose != null ? purpose : "VOTE", ipAddress, deviceFingerprint);

            if (success) {
                return Result.success("验证码发送成功");
            } else {
                return Result.error("验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("发送短信验证码异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/email/send")
    public Result<String> sendEmailCode(@RequestParam String emailAddress,
                                         @RequestParam(required = false) String purpose,
                                         HttpServletRequest request) {
        try {
            String ipAddress = getClientIp(request);
            String deviceFingerprint = request.getHeader("X-Device-Fingerprint");

            boolean success = verificationCodeService.sendCode("EMAIL", emailAddress,
                purpose != null ? purpose : "VOTE", ipAddress, deviceFingerprint);

            if (success) {
                return Result.success("验证码发送成功");
            } else {
                return Result.error("验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("发送邮箱验证码异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/code/verify")
    public Result<Boolean> verifyCode(@RequestBody VerifyCodeRequest request) {
        try {
            boolean success = verificationCodeService.verifyCode(request);
            return Result.success(success);
        } catch (Exception e) {
            log.error("验证验证码异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 检查IP是否被封禁
     */
    @GetMapping("/ip/check")
    public Result<Boolean> checkIpBlocked(@RequestParam String ipAddress) {
        try {
            boolean blocked = ipBlacklistService.isBlocked(ipAddress);
            return Result.success(blocked);
        } catch (Exception e) {
            log.error("检查IP封禁状态异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 添加IP到黑名单
     */
    @PostMapping("/ip/blacklist")
    public Result<String> addToBlacklist(@RequestBody Map<String, Object> params,
                                          HttpServletRequest request) {
        try {
            String ipAddress = (String) params.get("ipAddress");
            String reason = (String) params.getOrDefault("reason", "违规投票");
            String type = (String) params.getOrDefault("type", "TEMPORARY");
            Integer hours = params.get("hours") != null ? Integer.parseInt(params.get("hours").toString()) : 24;
            String operator = (String) params.getOrDefault("operator", "admin");
            String remark = (String) params.getOrDefault("remark", "");

            boolean success = ipBlacklistService.blockIp(ipAddress, reason, type, hours, operator, remark);
            if (success) {
                return Result.success("IP已加入黑名单");
            } else {
                return Result.error("操作失败，IP可能已在黑名单中");
            }
        } catch (Exception e) {
            log.error("添加IP黑名单异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 解封IP
     */
    @PostMapping("/ip/unblock")
    public Result<String> unblockIp(@RequestParam Long id) {
        try {
            boolean success = ipBlacklistService.unblockIp(id);
            if (success) {
                return Result.success("IP已解封");
            } else {
                return Result.error("解封失败");
            }
        } catch (Exception e) {
            log.error("解封IP异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 获取所有黑名单
     */
    @GetMapping("/ip/blacklist")
    public Result<List<IpBlacklist>> getBlacklist() {
        try {
            List<IpBlacklist> list = ipBlacklistService.getAllBlacklist();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取黑名单异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 生成设备指纹
     */
    @PostMapping("/device/fingerprint")
    public Result<String> generateFingerprint(@RequestBody Map<String, String> params) {
        try {
            String userAgent = params.get("userAgent");
            String acceptLanguage = params.get("acceptLanguage");
            String screenResolution = params.get("screenResolution");
            String timezone = params.get("timezone");

            String fingerprint = deviceFingerprintService.generateFingerprint(
                userAgent, acceptLanguage, screenResolution, timezone);

            if (fingerprint != null) {
                return Result.success(fingerprint);
            } else {
                return Result.error("生成设备指纹失败");
            }
        } catch (Exception e) {
            log.error("生成设备指纹异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 检测异常投票
     */
    @PostMapping("/fraud/detect")
    public Result<Map<String, Object>> detectFraud(@RequestBody Map<String, Object> params) {
        try {
            Long activityId = params.get("activityId") != null ?
                Long.parseLong(params.get("activityId").toString()) : null;
            String ipAddress = (String) params.get("ipAddress");
            String deviceFingerprint = (String) params.get("deviceFingerprint");

            FraudDetectionService.FraudAnalysisResult result = fraudDetectionService.analyzeVotingPattern(
                activityId, ipAddress, deviceFingerprint);

            Map<String, Object> data = new HashMap<>();
            data.put("suspicious", result.isSuspicious());
            data.put("fraudType", result.getFraudType());
            data.put("riskLevel", result.getRiskLevel());
            data.put("description", result.getDescription());

            return Result.success(data);
        } catch (Exception e) {
            log.error("检测异常投票异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 获取验证码发送统计
     */
    @GetMapping("/code/stats")
    public Result<List<VerificationCodeStats>> getCodeStats(@RequestParam(required = false, defaultValue = "1") Integer hours) {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
            List<VerificationCodeStats> stats = verificationCodeMapper.getSendStatsByIp(startTime, 100);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取验证码统计异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 获取设备使用统计
     */
    @GetMapping("/device/stats")
    public Result<List<DeviceStatsDto>> getDeviceStats() {
        try {
            // 注意：这里需要将DeviceFingerprintServiceImpl.DeviceStats暴露为公共接口
            // 暂时返回空列表
            return Result.success(List.of());
        } catch (Exception e) {
            log.error("获取设备统计异常", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
