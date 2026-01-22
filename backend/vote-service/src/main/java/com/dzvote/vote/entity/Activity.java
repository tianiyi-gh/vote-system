package com.dzvote.vote.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动实体
 */
@Data
public class Activity {
    /**
     * 活动ID
     */
    private Long id;

    /**
     * 活动唯一ID
     */
    private String serviceId;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 区域
     */
    private String region;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态：0-未开始，1-进行中，2-已结束
     */
    private Integer status;

    /**
     * 分组数量
     */
    private Integer groupCount;

    /**
     * 短信权重
     */
    private Integer smsWeight;

    /**
     * 语音权重
     */
    private Integer ivrWeight;

    /**
     * 网络权重
     */
    private Integer webWeight;

    /**
     * APP权重
     */
    private Integer appWeight;

    /**
     * 微信权重
     */
    private Integer wechatWeight;

    /**
     * 付费权重
     */
    private Integer payWeight;

    /**
     * IP限制
     */
    private Integer ipLimit;

    /**
     * 是否启用验证码
     */
    private Integer enableCaptcha;

    /**
     * 是否显示票数
     */
    private Integer showVotes;

    /**
     * 域名
     */
    private String domain;

    /**
     * 模板
     */
    private String template;

    /**
     * 规则
     */
    private String rules;

    /**
     * 投票限制（每人可投票数）
     */
    private Integer voteLimit;

    /**
     * 每天最多对多少名候选人投票（例如：5）
     * 0表示不限制
     */
    private Integer dailyCandidateLimit;

    /**
     * 每个候选人每天限投多少次（例如：1）
     * 0表示不限制
     */
    private Integer candidateDailyLimit;

    /**
     * 候选人数量（统计字段）
     */
    private Integer candidateCount;

    /**
     * 总票数（统计字段）
     */
    private Integer totalVotes;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    private Integer deleted;
}
