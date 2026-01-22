package com.dzvote.vote.service.impl;

import com.dzvote.vote.entity.VoteRecord;
import com.dzvote.vote.mapper.VoteRecordMapper;
import com.dzvote.vote.service.DeviceFingerprintService;
import com.dzvote.vote.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异常投票检测服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final VoteRecordMapper voteRecordMapper;
    private final DeviceFingerprintService deviceFingerprintService;

    /**
     * 检测投票是否异常
     */
    @Override
    public boolean detectFraud(VoteRecord voteRecord) {
        try {
            FraudAnalysisResult result = analyzeVotingPattern(
                voteRecord.getActivityId(),
                voteRecord.getVoterIp(),
                voteRecord.getDeviceInfo()
            );

            if (result.isSuspicious()) {
                log.warn("检测到异常投票: Activity={}, IP={}, Device={}, Type={}, Risk={}",
                    voteRecord.getActivityId(),
                    voteRecord.getVoterIp(),
                    voteRecord.getDeviceInfo(),
                    result.getFraudType(),
                    result.getRiskLevel());
            }

            return result.isSuspicious();

        } catch (Exception e) {
            log.error("异常投票检测失败", e);
            return false;
        }
    }

    /**
     * 分析投票模式
     */
    @Override
    public FraudAnalysisResult analyzeVotingPattern(Long activityId, String ipAddress, String deviceFingerprint) {
        FraudAnalysisResult result = new FraudAnalysisResult();

        try {
            // 检查1: IP短时间高频投票（1分钟内>10票）
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            int ipVoteCount = voteRecordMapper.countVotesByIpAndTime(ipAddress, oneMinuteAgo);

            if (ipVoteCount > 10) {
                result.setSuspicious(true);
                result.setFraudType("HIGH_FREQUENCY_IP");
                result.setRiskLevel("HIGH");
                result.setDescription(String.format("IP在1分钟内投票%d次", ipVoteCount));
                return result;
            }

            // 检查2: 设备短时间高频投票
            if (deviceFingerprint != null && !deviceFingerprint.isEmpty()) {
                int deviceVoteCount = voteRecordMapper.countVotesByDeviceAndTime(deviceFingerprint, oneMinuteAgo);

                if (deviceVoteCount > 10) {
                    result.setSuspicious(true);
                    result.setFraudType("HIGH_FREQUENCY_DEVICE");
                    result.setRiskLevel("HIGH");
                    result.setDescription(String.format("设备在1分钟内投票%d次", deviceVoteCount));
                    return result;
                }
            }

            // 检查3: IP在短时间内为多个不同候选人投票（1分钟内>5个不同候选人）
            List<Map<String, Object>> recentVotes = voteRecordMapper.findRecentVotesByIp(ipAddress, oneMinuteAgo);
            long distinctCandidateCount = recentVotes.stream()
                .map(v -> v.get("candidate_id"))
                .distinct()
                .count();

            if (distinctCandidateCount > 5) {
                result.setSuspicious(true);
                result.setFraudType("RASH_VOTING");
                result.setRiskLevel("MEDIUM");
                result.setDescription(String.format("IP在1分钟内为%d个不同候选人投票", distinctCandidateCount));
                return result;
            }

            // 检查4: 设备指纹异常
            if (deviceFingerprint != null && deviceFingerprintService.isSuspicious(deviceFingerprint)) {
                result.setSuspicious(true);
                result.setFraudType("SUSPICIOUS_DEVICE");
                result.setRiskLevel("MEDIUM");
                result.setDescription("设备指纹异常，可能为机器人/爬虫");
                return result;
            }

            // 检查5: IP在短时间内集中投票（如每秒1票）
            LocalDateTime oneSecondAgo = LocalDateTime.now().minusSeconds(1);
            int ipVoteCountPerSecond = voteRecordMapper.countVotesByIpAndTime(ipAddress, oneSecondAgo);

            if (ipVoteCountPerSecond >= 1) {
                result.setSuspicious(true);
                result.setFraudType("AUTOMATED_VOTING");
                result.setRiskLevel("HIGH");
                result.setDescription("检测到自动化投票行为");
                return result;
            }

            // 未发现异常
            result.setSuspicious(false);
            result.setRiskLevel("LOW");
            return result;

        } catch (Exception e) {
            log.error("分析投票模式异常", e);
            result.setSuspicious(false);
            result.setRiskLevel("LOW");
            return result;
        }
    }
}
