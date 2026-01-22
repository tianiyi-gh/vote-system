package com.dzvote.vote.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 候选人实体
 */
@Data
public class Candidate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动Service ID
     */
    private String serviceId;

    /**
     * 候选人编号
     */
    private String candidateNo;

    /**
     * 候选人姓名
     */
    private String name;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 描述
     */
    private String description;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 票数（兼容旧字段）
     */
    private Long votes;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 创建时间（兼容旧字段）
     */
    private LocalDateTime createdAt;

    /**
     * 短信票数
     */
    private Long smsVotes;

    /**
     * 语音票数
     */
    private Long ivrVotes;

    /**
     * 网络票数
     */
    private Long webVotes;

    /**
     * APP票数
     */
    private Long appVotes;

    /**
     * 微信票数
     */
    private Long wechatVotes;

    /**
     * 付费票数
     */
    private Long payVotes;

    /**
     * 总票数
     */
    private Long totalVotes;

    /**
     * 得分
     */
    private BigDecimal score;

    /**
     * 排名
     */
    private Integer ranking;

    /**
     * 状态：0-未开始，1-正常，2-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    private Integer deleted;
}
