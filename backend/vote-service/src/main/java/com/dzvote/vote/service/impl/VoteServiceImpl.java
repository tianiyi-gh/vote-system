package com.dzvote.vote.service.impl;

import com.dzvote.vote.entity.VoteRecord;
import com.dzvote.vote.entity.Activity;
import com.dzvote.vote.mapper.VoteRecordMapper;
import com.dzvote.vote.mapper.VoteLimitMapper;
import com.dzvote.vote.mapper.ActivityMapper;
import com.dzvote.vote.service.VoteService;
import com.dzvote.vote.dto.VoteRequest;
import com.dzvote.vote.dto.VoteResult;
import com.dzvote.vote.util.CaptchaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 投票服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final VoteRecordMapper voteRecordMapper;
    private final VoteLimitMapper voteLimitMapper;
    private final ActivityMapper activityMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;

    private static final String VOTE_LIMIT_KEY_PREFIX = "vote:limit:";
    private static final String PERSON_VOTE_KEY_PREFIX = "vote:person:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VoteResult vote(VoteRequest request) {
        try {
            // 1. 验证投票有效性
            if (!validateVote(request)) {
                return VoteResult.failure("INVALID_VOTE", "投票信息无效");
            }

            // 2. 验证验证码（如果活动启用验证码）
            Activity activity = activityMapper.findById(request.getActivityId());
            if (activity == null) {
                return VoteResult.failure("ACTIVITY_NOT_FOUND", "活动不存在");
            }

            if (activity.getEnableCaptcha() != null && activity.getEnableCaptcha() == 1) {
                // 支持滑块验证码和普通验证码
                if (request.getCaptcha() == null) {
                    return VoteResult.failure("CAPTCHA_REQUIRED", "请完成验证码验证");
                }

                // 滑块验证码通过标记
                if ("SLIDER_PASSED".equals(request.getCaptcha())) {
                    log.debug("滑块验证码通过: key={}", request.getCaptchaKey());
                } else if (request.getCaptchaKey() != null) {
                    // 普通验证码验证
                    boolean captchaValid = CaptchaUtil.verifyCaptcha(
                        request.getCaptchaKey(),
                        request.getCaptcha(),
                        stringRedisTemplate
                    );
                    if (!captchaValid) {
                        log.warn("验证码验证失败: key={}, input={}", request.getCaptchaKey(), request.getCaptcha());
                        return VoteResult.failure("CAPTCHA_ERROR", "验证码错误");
                    }
                } else {
                    return VoteResult.failure("CAPTCHA_REQUIRED", "请完成验证码验证");
                }
            }

            // 3. 检查每人投票限制
            if (!checkPersonVoteLimit(request, activity.getVoteLimit())) {
                return VoteResult.failure("PERSON_LIMIT_EXCEEDED",
                    "您已达到该活动的投票限制（每人" + activity.getVoteLimit() + "票）");
            }

            // 3.5. 检查每天最多对多少个候选人投票的限制
            if (!checkDailyCandidateLimit(request, activity.getDailyCandidateLimit())) {
                return VoteResult.failure("DAILY_CANDIDATE_LIMIT_EXCEEDED",
                    "您今天已达到最多可投候选人数限制（每天最多对" + activity.getDailyCandidateLimit() + "名候选人投票）");
            }

            // 3.6. 检查每个候选人每天限投多少次的限制
            if (!checkCandidateDailyLimit(request, activity.getCandidateDailyLimit())) {
                return VoteResult.failure("CANDIDATE_DAILY_LIMIT_EXCEEDED",
                    "您今天对该候选人的投票次数已达到限制（每个候选人每天限投" + activity.getCandidateDailyLimit() + "次）");
            }

            // 4. 检查IP投票限制
            if (!checkIpVoteLimit(request, activity.getIpLimit())) {
                return VoteResult.failure("IP_LIMIT_EXCEEDED",
                    "该IP地址投票次数已达到限制");
            }

            // 5. 创建投票记录
            VoteRecord voteRecord = buildVoteRecord(request);
            voteRecordMapper.insert(voteRecord);

            // 6. 更新候选人票数（总票数和对应渠道票数）
            incrementCandidateVotesByChannel(request.getCandidateId(), request.getChannel());

            // 7. 更新限制计数
            incrementVoteLimit(request);

            log.info("投票成功: activityId={}, candidateId={}, channel={}, voterPhone={}",
                    request.getActivityId(), request.getCandidateId(),
                    request.getChannel(), voteRecord.getVoterPhone());

            return VoteResult.success(voteRecord.getId());

        } catch (Exception e) {
            log.error("投票失败", e);
            return VoteResult.failure("SYSTEM_ERROR", "系统错误，请稍后重试");
        }
    }

    @Override
    public boolean checkVoteLimit(VoteRequest request) {
        // 获取活动的投票限制配置
        Activity activity = activityMapper.findById(request.getActivityId());
        if (activity == null) {
            log.warn("活动不存在: activityId={}", request.getActivityId());
            return false;
        }

        int voteLimit = activity.getVoteLimit() != null ? activity.getVoteLimit() : 10; // 默认每人10票

        // 检查IP限制 - 基于活动的IP限流
        String ipKey = VOTE_LIMIT_KEY_PREFIX + "ip:" + request.getActivityId() + ":" + request.getVoterIp();
        Long ipCount = redisTemplate.opsForValue().increment(ipKey);
        if (ipCount != null) {
            if (ipCount == 1) {
                redisTemplate.expire(ipKey, 1, TimeUnit.DAYS);
            }

            if (ipCount > voteLimit) {
                redisTemplate.opsForValue().decrement(ipKey);
                log.info("IP投票超限: activityId={}, ip={}, count={}, limit={}",
                        request.getActivityId(), request.getVoterIp(), ipCount, voteLimit);
                return false;
            }
        }

        log.debug("IP投票检查通过: activityId={}, ip={}, count={}",
                request.getActivityId(), request.getVoterIp(), ipCount);

        return true;
    }

    /**
     * 检查每人投票限制
     */
    private boolean checkPersonVoteLimit(VoteRequest request, Integer voteLimit) {
        if (request.getVoterId() == null) {
            return true; // 未提供voterId时不限制
        }

        String personKey = PERSON_VOTE_KEY_PREFIX + request.getActivityId() + ":" + request.getVoterId();
        Object countObj = redisTemplate.opsForValue().get(personKey);
        if (countObj == null) {
            return true; // 第一次投票
        }

        Long count = countObj instanceof Number ? ((Number) countObj).longValue() : 0L;
        if (count >= voteLimit) {
            log.info("每人投票限制: activityId={}, voterId={}, count={}, limit={}",
                    request.getActivityId(), request.getVoterId(), count, voteLimit);
            return false;
        }

        return true;
    }

    /**
     * 检查IP投票限制
     */
    private boolean checkIpVoteLimit(VoteRequest request, Integer ipLimit) {
        if (ipLimit == null || ipLimit <= 0) {
            return true; // 未设置IP限制
        }

        String ipKey = VOTE_LIMIT_KEY_PREFIX + "ip:" + request.getActivityId() + ":" + request.getVoterIp();
        Object ipCountObj = redisTemplate.opsForValue().get(ipKey);
        if (ipCountObj == null) {
            return true; // 第一次投票
        }

        Long ipCount = ipCountObj instanceof Number ? ((Number) ipCountObj).longValue() : 0L;
        if (ipCount >= ipLimit) {
            log.info("IP投票限制: activityId={}, ip={}, count={}, limit={}",
                    request.getActivityId(), request.getVoterIp(), ipCount, ipLimit);
            return false;
        }

        return true;
    }

    @Override
    public boolean validateVote(VoteRequest request) {
        // 验证渠道有效性
        return java.util.List.of("SMS", "IVR", "WEB", "APP", "WECHAT", "PAY").contains(request.getChannel());
    }

    private VoteRecord buildVoteRecord(VoteRequest request) {
        VoteRecord record = new VoteRecord();
        record.setActivityId(request.getActivityId());
        record.setCandidateId(request.getCandidateId());
        record.setChannel(request.getChannel());
        record.setVoterPhone(request.getVoterPhone());
        record.setVoterIp(request.getVoterIp());
        record.setVoterName(request.getVoterPhone()); // 使用手机号作为姓名
        record.setVoteTime(LocalDateTime.now());
        record.setValid(1);
        return record;
    }

    private void incrementVoteLimit(VoteRequest request) {
        // 增加每人投票计数
        if (request.getVoterId() != null) {
            String personKey = PERSON_VOTE_KEY_PREFIX + request.getActivityId() + ":" + request.getVoterId();
            redisTemplate.opsForValue().increment(personKey);
            // 设置过期时间为1天
            redisTemplate.expire(personKey, 1, TimeUnit.DAYS);
        }

        // 增加IP投票计数
        String ipKey = VOTE_LIMIT_KEY_PREFIX + "ip:" + request.getActivityId() + ":" + request.getVoterIp();
        redisTemplate.opsForValue().increment(ipKey);
        // 设置过期时间为1天
        redisTemplate.expire(ipKey, 1, TimeUnit.DAYS);
    }

    /**
     * 根据渠道增加候选人票数
     */
    private void incrementCandidateVotesByChannel(Long candidateId, String channel) {
        String column = switch (channel) {
            case "SMS" -> "sms_votes";
            case "IVR" -> "ivr_votes";
            case "WEB" -> "web_votes";
            case "APP" -> "app_votes";
            case "WECHAT" -> "wechat_votes";
            case "PAY" -> "pay_votes";
            default -> throw new IllegalArgumentException("不支持的渠道: " + channel);
        };

        voteRecordMapper.incrementCandidateVotes(candidateId);
        voteRecordMapper.incrementCandidateVotesByChannel(candidateId, column);
    }

    /**
     * 检查每天最多对多少个候选人投票的限制
     */
    private boolean checkDailyCandidateLimit(VoteRequest request, Integer dailyCandidateLimit) {
        if (dailyCandidateLimit == null || dailyCandidateLimit <= 0) {
            return true; // 未设置限制
        }

        // 统计用户今天对多少个不同候选人投过票
        Integer votedCount = voteRecordMapper.countTodayVotedCandidates(
            request.getActivityId(),
            request.getVoterPhone(),
            request.getVoterIp()
        );

        if (votedCount != null && votedCount >= dailyCandidateLimit) {
            log.info("每天最多候选人限制: activityId={}, voterPhone={}, votedCount={}, limit={}",
                    request.getActivityId(), request.getVoterPhone(), votedCount, dailyCandidateLimit);
            return false;
        }

        return true;
    }

    /**
     * 检查每个候选人每天限投多少次的限制
     */
    private boolean checkCandidateDailyLimit(VoteRequest request, Integer candidateDailyLimit) {
        if (candidateDailyLimit == null || candidateDailyLimit <= 0) {
            return true; // 未设置限制
        }

        // 统计用户今天对该候选人投了多少票
        Integer voteCount = voteRecordMapper.countTodayVotesForCandidate(
            request.getActivityId(),
            request.getCandidateId(),
            request.getVoterPhone(),
            request.getVoterIp()
        );

        if (voteCount != null && voteCount >= candidateDailyLimit) {
            log.info("候选人每天限投限制: activityId={}, candidateId={}, voterPhone={}, voteCount={}, limit={}",
                    request.getActivityId(), request.getCandidateId(), request.getVoterPhone(), voteCount, candidateDailyLimit);
            return false;
        }

        return true;
    }
}
