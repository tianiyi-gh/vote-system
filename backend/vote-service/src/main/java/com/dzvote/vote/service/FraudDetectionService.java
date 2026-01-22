package com.dzvote.vote.service;

import com.dzvote.vote.entity.VoteRecord;

/**
 * 异常投票检测服务
 */
public interface FraudDetectionService {

    /**
     * 检测投票是否异常
     * @param voteRecord 投票记录
     * @return true-异常 false-正常
     */
    boolean detectFraud(VoteRecord voteRecord);

    /**
     * 分析投票模式
     * @param activityId 活动ID
     * @param ipAddress IP地址
     * @param deviceFingerprint 设备指纹
     * @return 分析结果
     */
    FraudAnalysisResult analyzeVotingPattern(Long activityId, String ipAddress, String deviceFingerprint);

    /**
     * 异常投票分析结果
     */
    class FraudAnalysisResult {
        /**
         * 是否异常
         */
        private boolean suspicious;

        /**
         * 异常类型
         */
        private String fraudType;

        /**
         * 风险等级：LOW/MEDIUM/HIGH
         */
        private String riskLevel;

        /**
         * 异常描述
         */
        private String description;

        public boolean isSuspicious() { return suspicious; }
        public void setSuspicious(boolean suspicious) { this.suspicious = suspicious; }
        public String getFraudType() { return fraudType; }
        public void setFraudType(String fraudType) { this.fraudType = fraudType; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
