package com.dzvote.vote.service.impl;

import com.dzvote.vote.dto.VerifyCodeRequest;
import com.dzvote.vote.entity.VerificationCode;
import com.dzvote.vote.mapper.VerificationCodeMapper;
import com.dzvote.vote.service.EmailService;
import com.dzvote.vote.service.SmsService;
import com.dzvote.vote.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 验证码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeMapper verificationCodeMapper;
    private final SmsService smsService;
    private final EmailService emailService;

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int FREQUENT_LIMIT = 10; // 1小时内最多发送10次
    private static final int FREQUENT_WINDOW_HOURS = 1;

    private final SecureRandom random = new SecureRandom();

    /**
     * 生成随机验证码
     */
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送验证码
     */
    @Override
    public boolean sendCode(String type, String target, String purpose, String ipAddress, String deviceFingerprint) {
        try {
            // 1. 检查发送频率
            if (isFrequentSender(ipAddress)) {
                log.warn("频繁发送验证码检测: IP={}, Device={}", ipAddress, deviceFingerprint);
                return false;
            }

            // 2. 检查目标格式
            if (!validateTarget(type, target)) {
                log.warn("无效的目标格式: Type={}, Target={}", type, target);
                return false;
            }

            // 3. 生成验证码
            String code = generateCode();

            // 4. 保存验证码记录
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setType(type);
            verificationCode.setTarget(target);
            verificationCode.setCode(code);
            verificationCode.setPurpose(purpose);
            verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
            verificationCode.setCreateTime(LocalDateTime.now());
            verificationCode.setIpAddress(ipAddress);
            verificationCode.setDeviceFingerprint(deviceFingerprint);

            int result = verificationCodeMapper.insert(verificationCode);
            if (result <= 0) {
                log.error("保存验证码记录失败");
                return false;
            }

            // 5. 发送验证码
            boolean sent = false;
            if ("SMS".equals(type)) {
                sent = smsService.sendVerificationCode(target, code);
            } else if ("EMAIL".equals(type)) {
                sent = emailService.sendVerificationCode(target, code);
            }

            if (!sent) {
                log.error("发送验证码失败: Type={}, Target={}", type, target);
                return false;
            }

            log.info("验证码发送成功: Type={}, Target={}, Code={}, IP={}", type, target, code, ipAddress);
            return true;

        } catch (Exception e) {
            log.error("发送验证码异常", e);
            return false;
        }
    }

    /**
     * 验证验证码
     */
    @Override
    public boolean verifyCode(VerifyCodeRequest request) {
        try {
            // 1. 查找最新的未验证验证码
            VerificationCode verificationCode = verificationCodeMapper.findLatestUnverified(
                request.getTarget(), request.getPurpose());

            if (verificationCode == null) {
                log.warn("未找到未验证的验证码: Target={}, Purpose={}", request.getTarget(), request.getPurpose());
                return false;
            }

            // 2. 验证验证码是否匹配
            if (!verificationCode.getCode().equals(request.getCode())) {
                log.warn("验证码不匹配: Expected={}, Actual={}", verificationCode.getCode(), request.getCode());
                return false;
            }

            // 3. 验证是否过期
            if (verificationCode.getExpireTime().isBefore(LocalDateTime.now())) {
                log.warn("验证码已过期: ExpireTime={}", verificationCode.getExpireTime());
                return false;
            }

            // 4. 标记为已验证
            int result = verificationCodeMapper.markAsVerified(verificationCode.getId(), LocalDateTime.now());
            if (result <= 0) {
                log.error("标记验证码失败");
                return false;
            }

            log.info("验证码验证成功: Target={}, Code={}", request.getTarget(), request.getCode());
            return true;

        } catch (Exception e) {
            log.error("验证验证码异常", e);
            return false;
        }
    }

    /**
     * 检查IP是否频繁发送验证码
     */
    @Override
    public boolean isFrequentSender(String ipAddress) {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusHours(FREQUENT_WINDOW_HOURS);
            int count = verificationCodeMapper.countRecentCodes(ipAddress, null, startTime);
            return count >= FREQUENT_LIMIT;
        } catch (Exception e) {
            log.error("检查频繁发送异常", e);
            return false;
        }
    }

    /**
     * 验证目标格式
     */
    private boolean validateTarget(String type, String target) {
        if (target == null || target.isEmpty()) {
            return false;
        }

        if ("SMS".equals(type)) {
            // 手机号格式验证：11位数字，1开头
            return target.matches("^1[3-9]\\d{9}$");
        } else if ("EMAIL".equals(type)) {
            // 邮箱格式验证
            return target.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        }

        return false;
    }
}
