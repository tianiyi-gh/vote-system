package com.dzvote.statistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 投票统计实体
 */
@Data
@TableName("vote_statistics")
public class VoteStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 总投票数
     */
    private Long totalVotes;

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
     * 独立投票人数
     */
    private Integer uniqueVoters;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}