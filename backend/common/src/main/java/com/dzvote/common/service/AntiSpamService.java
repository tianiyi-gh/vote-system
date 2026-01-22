package com.dzvote.common.service;

import com.dzvote.vote.dto.VoteRequest;

/**
 * 防刷票服务接口
 */
public interface AntiSpamService {

    /**
     * 检查是否允许投票
     * @param voteRequest 投票请求
     * @return 检查结果
     */
    AntiSpamResult checkVoteAllowed(VoteRequest voteRequest);

    /**
     * 记录投票行为
     * @param voteRequest 投票请求
     */
    void recordVote(VoteRequest voteRequest);

    /**
     * 检查IP是否在黑名单
     * @param ip IP地址
     * @return 是否在黑名单
     */
    boolean isIPBlacklisted(String ip);

    /**
     * 检查设备是否被限制
     * @param deviceId 设备ID
     * @return 是否被限制
     */
    boolean isDeviceRestricted(String deviceId);

    /**
     * 防刷票检查结果
     */
    class AntiSpamResult {
        private boolean allowed;
        private String reason;
        private String errorCode;

        public AntiSpamResult(boolean allowed, String reason, String errorCode) {
            this.allowed = allowed;
            this.reason = reason;
            this.errorCode = errorCode;
        }

        public static AntiSpamResult success() {
            return new AntiSpamResult(true, null, null);
        }

        public static AntiSpamResult failure(String reason, String errorCode) {
            return new AntiSpamResult(false, reason, errorCode);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}