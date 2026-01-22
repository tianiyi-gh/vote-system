package com.dzvote.vote.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投票记录实体
 */
@Data
public class VoteRecord implements Serializable {

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
     * 候选人ID
     */
    private Long candidateId;

    /**
     * 投票渠道：SMS/IVR/WEB/APP/WECHAT/PAY
     */
    private String channel;

    /**
     * 投票人姓名
     */
    private String voterName;

    /**
     * 投票人手机号
     */
    private String voterPhone;

    /**
     * 投票人IP
     */
    private String voterIp;

    /**
     * 投票时间
     */
    private LocalDateTime voteTime;

    /**
     * 是否有效：0-无效 1-有效
     */
    private Integer valid;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 位置信息
     */
    private String location;

    /**
     * 候选人名称（用于展示）
     */
    private String candidateName;

    /**
     * 候选人头像（用于展示）
     */
    private String candidateAvatar;

    /**
     * 候选人编号（用于展示）
     */
    private Integer candidateNo;
}