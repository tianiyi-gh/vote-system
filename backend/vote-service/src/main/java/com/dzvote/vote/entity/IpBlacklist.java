package com.dzvote.vote.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IP黑名单实体
 */
@Data
public class IpBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 封禁类型：PERMANENT/TEMPORARY
     */
    private String type;

    /**
     * 封禁开始时间
     */
    private LocalDateTime startTime;

    /**
     * 封禁结束时间
     */
    private LocalDateTime endTime;

    /**
     * 封禁状态：0-已解除 1-生效中
     */
    private Integer status;

    /**
     * 操作员
     */
    private String operator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;
}
