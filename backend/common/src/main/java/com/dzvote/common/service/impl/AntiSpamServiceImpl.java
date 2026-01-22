package com.dzvote.common.service.impl;

import com.dzvote.common.service.AntiSpamService;
import com.dzvote.vote.dto.VoteRequest;
import com.dzvote.activity.entity.Activity;
import com.dzvote.activity.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 防刷票服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntiSpamServiceImpl implements AntiSpamService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ActivityService activityService;

    // Redis Key前缀
    private static final String VOTE_LIMIT_PREFIX = "vote_limit:";
    private static final String IP_BLACKLIST_PREFIX = "ip_blacklist:";
    private static final String DEVICE_RESTRICT_PREFIX = "device_restrict:";
    private static final String FINGERPRINT_PREFIX = "fingerprint:";
    
    // 限制时间窗口
    private static final Duration IP_WINDOW = Duration.ofHours(24);
    private static final Duration DEVICE_WINDOW = Duration.ofHours(24);
    private static final Duration FINGERPRINT_WINDOW = Duration.ofMinutes(30);
    
    // 黑名单时间
    private static final Duration BLACKLIST_DURATION = Duration.ofDays(7);

    @Override
    public AntiSpamResult checkVoteAllowed(VoteRequest voteRequest) {
        try {
            // 1. 检查活动是否存在且有效
            Activity activity = activityService.getById(voteRequest.getActivityId());
            if (activity == null || activity.getStatus() != 1) {
                return AntiSpamResult.failure("活动不存在或已结束", "ACTIVITY_INVALID");
            }

            // 2. 检查IP黑名单
            if (isIPBlacklisted(voteRequest.getVoterIp())) {
                return AntiSpamResult.failure("IP已被限制", "IP_BLACKLISTED");
            }

            // 3. 检查设备限制
            String deviceId = getDeviceId(voteRequest);
            if (isDeviceRestricted(deviceId)) {
                return AntiSpamResult.failure("设备已被限制", "DEVICE_RESTRICTED");
            }

            // 4. 检查IP投票频率
            String ipLimitKey = VOTE_LIMIT_PREFIX + "ip:" + voteRequest.getActivityId() + ":" + voteRequest.getVoterIp();
            Long ipCount = redisTemplate.opsForValue().increment(ipLimitKey);
            
            if (ipCount != null) {
                if (ipCount == 1) {
                    redisTemplate.expire(ipLimitKey, IP_WINDOW);
                }
                
                int ipLimit = activity.getIpLimit() != null ? activity.getIpLimit() : 10;
                if (ipLimit > 0 && ipCount > ipLimit) {
                    log.warn("IP投票超限: ip={}, count={}, limit={}", voteRequest.getVoterIp(), ipCount, ipLimit);
                    return AntiSpamResult.failure("超出IP投票限制", "IP_LIMIT_EXCEEDED");
                }
            }

            // 5. 检查设备投票频率
            String deviceLimitKey = VOTE_LIMIT_PREFIX + "device:" + voteRequest.getActivityId() + ":" + deviceId;
            Long deviceCount = redisTemplate.opsForValue().increment(deviceLimitKey);
            
            if (deviceCount != null) {
                if (deviceCount == 1) {
                    redisTemplate.expire(deviceLimitKey, DEVICE_WINDOW);
                }
                
                if (deviceCount > 5) { // 设备每天最多5票
                    log.warn("设备投票超限: deviceId={}, count={}", deviceId, deviceCount);
                    return AntiSpamResult.failure("超出设备投票限制", "DEVICE_LIMIT_EXCEEDED");
                }
            }

            // 6. 检查指纹相似度（防止脚本刷票）
            String fingerprint = generateFingerprint(voteRequest);
            String fingerprintKey = FINGERPRINT_PREFIX + fingerprint;
            Long fingerprintCount = redisTemplate.opsForValue().increment(fingerprintKey);
            
            if (fingerprintCount != null) {
                if (fingerprintCount == 1) {
                    redisTemplate.expire(fingerprintKey, FINGERPRINT_WINDOW);
                }
                
                if (fingerprintCount > 20) { // 相同指纹30分钟内最多20次请求
                    log.warn("指纹请求过于频繁: fingerprint={}, count={}", fingerprint, fingerprintCount);
                    
                    // 加入黑名单
                    addToIPBlacklist(voteRequest.getVoterIp(), "频繁操作");
                    return AntiSpamResult.failure("操作过于频繁，请稍后再试", "FREQUENT_OPERATION");
                }
            }

            return AntiSpamResult.success();

        } catch (Exception e) {
            log.error("防刷票检查失败", e);
            return AntiSpamResult.failure("系统错误", "SYSTEM_ERROR");
        }
    }

    @Override
    public void recordVote(VoteRequest voteRequest) {
        try {
            // 记录投票行为用于分析
            String recordKey = "vote_record:" + voteRequest.getActivityId() + ":" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // 这里可以记录更详细的信息用于数据分析
            redisTemplate.opsForSet().add(recordKey, getDeviceId(voteRequest));
            redisTemplate.expire(recordKey, Duration.ofDays(30));
            
        } catch (Exception e) {
            log.error("记录投票行为失败", e);
        }
    }

    @Override
    public boolean isIPBlacklisted(String ip) {
        String key = IP_BLACKLIST_PREFIX + ip;
        return redisTemplate.hasKey(key);
    }

    @Override
    public boolean isDeviceRestricted(String deviceId) {
        String key = DEVICE_RESTRICT_PREFIX + deviceId;
        return redisTemplate.hasKey(key);
    }

    /**
     * 加入IP黑名单
     */
    public void addToIPBlacklist(String ip, String reason) {
        String key = IP_BLACKLIST_PREFIX + ip;
        redisTemplate.opsForValue().set(key, reason, BLACKLIST_DURATION);
        log.warn("IP加入黑名单: ip={}, reason={}", ip, reason);
    }

    /**
     * 限制设备
     */
    public void restrictDevice(String deviceId, String reason) {
        String key = DEVICE_RESTRICT_PREFIX + deviceId;
        redisTemplate.opsForValue().set(key, reason, BLACKLIST_DURATION);
        log.warn("设备被限制: deviceId={}, reason={}", deviceId, reason);
    }

    /**
     * 获取设备ID
     */
    private String getDeviceId(VoteRequest voteRequest) {
        // 组合多个信息生成设备ID
        String userAgent = ""; // 需要从请求头获取
        String voterId = voteRequest.getVoterId();
        String voterPhone = voteRequest.getVoterPhone();
        
        return DigestUtils.md5DigestAsHex(
            (voterId + voterPhone + userAgent).getBytes()
        );
    }

    /**
     * 生成请求指纹
     */
    private String generateFingerprint(VoteRequest voteRequest) {
        // 基于IP、时间窗口等生成指纹
        String timeWindow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String source = voteRequest.getVoterIp() + voteRequest.getChannel() + timeWindow;
        
        return DigestUtils.md5DigestAsHex(source.getBytes());
    }
}