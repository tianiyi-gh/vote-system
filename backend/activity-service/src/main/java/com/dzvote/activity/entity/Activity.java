package com.dzvote.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动实体类
 */
@Data
public class Activity {
    
    /**
     * 活动ID
     */
    private Long id;
    
    /**
     * 活动标题
     */
    private String title;
    
    /**
     * 活动描述
     */
    private String description;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 活动状态 (0:未开始, 1:进行中, 2:已结束)
     */
    private Integer status;
    
    /**
     * 每人投票限制
     */
    private Integer voteLimit;
    
    /**
     * 候选人数
     */
    private Integer candidateCount;
    
    /**
     * 总投票数
     */
    private Long totalVotes;
    
    /**
     * 活动Service ID
     */
    private String serviceId;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除标记 (0:未删除, 1:已删除)
     */
    private Integer deleted;
}
