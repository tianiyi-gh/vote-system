package com.dzvote.candidate.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 候选人实体类
 */
@Data
public class Candidate {

    /**
     * 候选人ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 候选人姓名
     */
    private String name;

    /**
     * 候选人描述
     */
    private String description;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 得票数
     */
    private Integer votes;

    /**
     * 候选人编号
     */
    private String candidateNo;

    /**
     * 活动Service ID
     */
    private String serviceId;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 状态 (1:正常, 2:禁用)
     */
    private Integer status;

    /**
     * 逻辑删除标记 (0:未删除, 1:已删除)
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
